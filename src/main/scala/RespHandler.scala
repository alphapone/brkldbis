package com.pmkitten.brkldbis

import scala.io._
import java.io._

class RespHandler(in:Iterator[String], out:PrintStream) {
  val commands:Map[String, RespCommand] = RespCommandsMap.commands

  private def hasNext = in.hasNext
  private def next() = in.next()

  private def readArray:Seq[String] = {
    if (!hasNext)
      Seq.empty
    else {
      val line = next()
      val argc = if (line.startsWith("*")) {
        line.substring(1).replaceAll("\r", "").toInt
      } else {
        0
      }
      if (argc==0 && line.startsWith("+"))
        Seq.apply(line.substring(1).replaceAll("\r", ""))
      else
        readArrayArgs(argc)
    }
  }

  private def readArrayArgs(argc:Int):Seq[String] = {
    if (argc==0)
      Seq.empty
    else {
      Seq.apply(readSomeStringArg).concat(readArrayArgs(argc-1))
    }
  }

  private def readSomeStringArg:String = {
      val line = next()
      if (line.startsWith("$")) {
        val arglen = line.substring(1).replaceAll("\r","").toInt
        var bl = 0
        var retVal = ""
        while (bl < arglen) {
          if (hasNext) {
            val line = next()
            bl = bl + line.getBytes.length + System.lineSeparator().length
            if (retVal.isEmpty)
              retVal = line
            else
              retVal = retVal + line
          } else {
            bl = arglen
          }
        }
        retVal
      } else {
        ""
      }
  }

  def handle():Unit = {
    try {
      val request = readArray
      if (request.length > 1) {
        val command = request.head
        val commandEvaluator = 
          commands.get(command)
        if (commandEvaluator.isDefined) {
          if (commandEvaluator.get!=null) {
            commandEvaluator.get.evaluate(request.tail, out)
          } else {
            out.print("-Unimplemented command\r\n")
          }
        } else {
          out.print(s"-Unknown command $command\r\n")
        }
      } else {
        out.print("-Command not found in request!\r\n")
      }
    } catch {
      case e:Exception=>
        out.print(s"-${e.getMessage}\r\n")
        out.flush()
    }
  }

}
