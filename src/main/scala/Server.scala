package com.pmkitten.brkldbis

import java.util.concurrent.{Executors, TimeUnit}
import scala.annotation.tailrec
import java.net._
import java.io._
import scala.io._

/**
 * brkldbis server
 * See RespCommandsMap file for implemented commands map
 */
object Server {

  val DEFAULT_PORT = 16384                         // use `--port' command line argumet to overwrite
  val DEFAULT_DB = "/tmp/com-pmkitten-brkldbis.db" // use `--db' command line argumet to overwrite

  @tailrec
  def getArg(args:Array[String], arg:String):Option[String] = {
    if (args==null || args.isEmpty || args.length<2 || arg==null || arg.isEmpty)
      Option.empty
    else if (args.head == arg) {
      Option.apply(args.tail.head)
    } else
      getArg(args.tail, arg)
  }

  @tailrec
  def handleLines(lines:Iterator[String], out:PrintStream):Unit = {
    if (lines.hasNext) {
      new RespHandler(lines, out).handle()
      out.flush()
      handleLines(lines, out)
    }
  }

  @tailrec
  def handleServerSocket(socket:ServerSocket):Unit = {
    if (socket!=null && !socket.isClosed) {
      val s = socket.accept()
      new Thread(()=>
        try {
          handleLines(BufferedSource(s.getInputStream).getLines(), new PrintStream(s.getOutputStream))
        } catch {
          case ignored:java.net.SocketException =>
          // connection reset
        }
        finally {
          s.close()
        }
      ).start()
      handleServerSocket(socket)
    }
  }

  def getPort(args:Array[String]):Int = getArg(args, "--port").map(x=>x.toInt).getOrElse(DEFAULT_PORT)
  def getDb(args:Array[String]):String = getArg(args, "--db").getOrElse(DEFAULT_DB)

  def main(args:Array[String]):Unit = {
    SaveMonitor.setDbName(getDb(args))
    val cronPool = Executors.newScheduledThreadPool(1)
    cronPool.schedule(new Thread(() => SaveMonitor.runSavingOnce()), 10, TimeUnit.SECONDS)
    handleServerSocket(new ServerSocket(getPort(args)))
    cronPool.shutdown()
  }
}
