package com.pmkitten.brkldbis

import java.io._

import com.sleepycat.je.{DatabaseConfig, Environment, EnvironmentConfig}

abstract class RespCommand {

  def evaluate(args:Seq[String] , out:PrintStream):Unit

}
