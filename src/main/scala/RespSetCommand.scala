package com.pmkitten.brkldbis

import com.sleepycat.je.*

import java.io.{File, PrintStream}

class RespSetCommand extends RespCommand {

  override def evaluate(args: Seq[String], out: PrintStream): Unit = {
    val aKey = args.head
    val aData = args(1)
    val aKeyBytes = aKey.getBytes("UTF-8")
    val aDataBytes = aData.getBytes("UTF-8")
    SaveMonitor.putTempVal(aKeyBytes, aDataBytes)
    out.print("+OK\r\n")
    SaveMonitor.runSavingOnce()
  }

}
