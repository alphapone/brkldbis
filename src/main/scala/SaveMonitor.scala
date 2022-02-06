package com.pmkitten.brkldbis

import com.sleepycat.je.*

import scala.collection.mutable
import java.io.{File, PrintStream}
import scala.collection.mutable

object SaveMonitor {
  val putgetTempStore = new mutable.HashMap[Array[Byte],Array[Byte]]

  def getTempVal(key:Array[Byte]):Option[Array[Byte]] = putgetTempStore.synchronized { putgetTempStore.get(key)}
  def putTempVal(key:Array[Byte], value:Array[Byte]):Unit = putgetTempStore.synchronized { putgetTempStore.put(key, value)}

  def deleteKey(key:Array[Byte]):Unit = {
    putgetTempStore.synchronized {
      putgetTempStore.remove(key)
    }
    val txnConfig = new TransactionConfig
    val txn = dbEnvCreated.get.beginTransaction(null, txnConfig)
    if (dbCreated.get.delete(txn, new DatabaseEntry(key)) eq OperationStatus.SUCCESS) {
      txn.commit()
    }
  }

  var saving:Boolean = false

  val envConfig = new EnvironmentConfig
  envConfig.setAllowCreate(true)
  envConfig.setSharedCache(true)
  envConfig.setTransactional(true)
  val dbConfig = new DatabaseConfig
  dbConfig.setAllowCreate(true)
  dbConfig.setTransactional(true)

  var dbEnvCreated:Option[Environment] = Option.empty
  var dbCreated:Option[Database] = Option.empty
  var dbName:String = ""

  def setDbName(dbName:String):Unit = {
    dbEnvCreated = Option.apply(new Environment(new File(dbName).getParentFile, envConfig))
    dbCreated = Option.apply(dbEnvCreated.get.openDatabase(null, dbName.tail, dbConfig))
    this.dbName = dbName
  }

  def getDB(dbName:String):Database = {
    dbCreated.get
  }

  def getVal(key:Array[Byte]):Option[Array[Byte]] = {
    val theKey = new DatabaseEntry(key)
    val theData = new DatabaseEntry
    val txnConfig = new TransactionConfig
    txnConfig.setReadOnly(true)
    txnConfig.setReadUncommitted(true)
    val txn = dbEnvCreated.get.beginTransaction(null, txnConfig)
    if (dbCreated.get.get(txn, theKey, theData, LockMode.DEFAULT) eq OperationStatus.SUCCESS) {
      txn.commit()
      Option.apply(theData.getData)
    } else {
      txn.commit()
      Option.empty
    }
  }

  private def requestSave():Boolean = {
    this.synchronized {
      val notSaving = !saving
      if (notSaving)
        saving=true
      notSaving
    }
  }

  private def releaseSave():Unit = {
    this.synchronized {
      saving = false
    }
  }

  def runSavingOnce():Unit = {
    if (requestSave()) {
      new Thread() {
        override def run():Unit = {
          try {
            val db = SaveMonitor.getDB(dbName)
            val txnConfig = new TransactionConfig
            db.synchronized {
              val toSave = putgetTempStore.synchronized {
                val seqtosave = putgetTempStore.toSeq
                putgetTempStore.clear()
                seqtosave
              }
              val txn = db.getEnvironment.beginTransaction(null, null)
              toSave.foreach(x => db.put(txn, new DatabaseEntry(x._1), new DatabaseEntry(x._2)))
              txn.commitSync()
              db.getEnvironment.evictMemory()
            }
          } finally {
            releaseSave()
          }
        }
      }.start()
    }
  }
}
