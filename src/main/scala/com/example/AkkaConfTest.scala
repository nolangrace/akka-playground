package com.example

import akka.actor.ActorSystem

object AkkaConfTest extends App {

  implicit val system = ActorSystem("my-system")

  system.logConfiguration()

}
