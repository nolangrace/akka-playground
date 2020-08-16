name := "akka-playground"

version := "1.0"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.6.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "2.0.0",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "com.typesafe.akka" %% "akka-http"   % "10.1.11",
  "com.github.alexandrnikitin" %% "bloom-filter" % "0.11.0"
)
