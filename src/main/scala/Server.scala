package com.pmkitten.brkldbis

import java.util.concurrent.{Executors, TimeUnit}
import scala.annotation.tailrec

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

  def getPort(args:Array[String]):Int = getArg(args, "--port").map(x=>x.toInt).getOrElse(DEFAULT_PORT)
  def getDb(args:Array[String]):String = getArg(args, "--db").getOrElse(DEFAULT_DB)

  def main(args:Array[String]):Unit = {
    import java.net._
    import java.io._
    import scala.io._

    val cronPool = Executors.newScheduledThreadPool(1)
    cronPool.schedule(new Thread(() => SaveMonitor.runSavingOnce()), 10, TimeUnit.SECONDS)

    SaveMonitor.setDbName(getDb(args))
    val server = new ServerSocket(getPort(args))
    while (true) {
      val s = server.accept()
      new Thread {
        override def run():Unit = {
          val in = BufferedSource(s.getInputStream)
          val out = new PrintStream(s.getOutputStream)
          val lines = in.getLines()
          try {
            while (lines.hasNext) {
              new RespHandler(lines, out).handle()
              out.flush()
            }
          } catch {
            case ignored:java.net.SocketException =>
              // connection reset
          }
          finally {
            s.close()
          }
        }
      }.start()
    }
  }
}


