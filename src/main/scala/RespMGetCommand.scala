package com.pmkitten.brkldbis

import com.sleepycat.je.*

import java.io.{File, PrintStream}

class RespMGetCommand extends RespCommand {

  override def evaluate(args: Seq[String], out: PrintStream): Unit = {
    out.print(s"*${args.length}\r\n")
    args.foreach(aKey=>{
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
    })
  }

}
