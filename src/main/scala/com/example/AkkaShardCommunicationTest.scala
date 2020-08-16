//package com.example
//
//import akka.actor.Actor
//
//object AkkaShardCommunicationTest extends App {
//
//  val counterRegion: ActorRef = ClusterSharding(system).start(
//    typeName = "Counter",
//    entityProps = Props[Counter],
//    settings = ClusterShardingSettings(system),
//    extractEntityId = extractEntityId,
//    extractShardId = extractShardId)
//
//
//  val counterRegion: ActorRef = ClusterSharding(system).shardRegion("Counter")
//  counterRegion ! Get(123)
//
//}
//
//
//class Counter extends Actor {
//
//  // self.path.name is the entity identifier (utf-8 URL-encoded)
//  override def persistenceId: String = "TestShardEntity-" + self.path.name
//
//  var count = 0
//
//  def updateState(event: CounterChanged): Unit =
//    count += event.delta
//
//  override def receiveRecover: Receive = {
//    case evt: CounterChanged => updateState(evt)
//  }
//
//  override def receiveCommand: Receive = {
//    case Increment      => persist(CounterChanged(+1))(updateState)
//    case Decrement      => persist(CounterChanged(-1))(updateState)
//    case Get(_)         => sender() ! count
//    case ReceiveTimeout => context.parent ! Passivate(stopMessage = Stop)
//    case Stop           => context.stop(self)
//  }
//}
