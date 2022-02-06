package com.pmkitten.brkldbis

object RespCommandsMap {
  val commands:Map[String, RespCommand] = Map(
    "GET" -> new RespGetCommand,
    "SET" -> new RespSetCommand,
    "MGET" -> new RespMGetCommand,
    "DEL" -> new RespDeleteCommand
  )
}
