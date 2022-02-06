package com.pmkitten.brkldbis

import com.sleepycat.je.*

import java.io.{File, PrintStream}

class RespDeleteCommand extends RespCommand {

  override def evaluate(args: Seq[String], out: PrintStream): Unit = {
    val deleted = args.map(aKey=> {
      val aKeyBytes = aKey.getBytes("UTF-8")
      if (SaveMonitor.getVal(aKeyBytes).isDefined) {
        SaveMonitor.deleteKey(aKeyBytes)
        1
      } else
        0
    }).sum
    out.print(s":$deleted\r\n")
    SaveMonitor.runSavingOnce()
  }
}
