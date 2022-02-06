package com.pmkitten.brkldbis
import java.io.PrintStream
import java.io.File
import com.sleepycat.je.{DatabaseConfig, Environment, EnvironmentConfig}
import com.sleepycat.je.LockMode
import com.sleepycat.je.OperationStatus
import com.sleepycat.je.DatabaseEntry

class RespGetCommand extends RespCommand {

  override def evaluate(args: Seq[String], out: PrintStream): Unit = {
    val aKey = args.head
    val aKeyBytes = aKey.getBytes("UTF-8")
    val aKeyTempVal = SaveMonitor.getTempVal(aKeyBytes)
    if (aKeyTempVal.isDefined) {
      val foundData = new String(aKeyTempVal.get, "UTF-8")
      out.print(s"$$${aKeyTempVal.get.length}\r\n$foundData\r\n")
    } else {
      val keyVal = SaveMonitor.getVal(aKeyBytes)
      if (keyVal.isDefined) {
        val foundData = new String(keyVal.get, "UTF-8")
        out.print(s"$$${keyVal.get.length}\r\n$foundData\r\n")
      } else {
        out.print(s"$$-1\r\n")
      }
    }
  }

}
