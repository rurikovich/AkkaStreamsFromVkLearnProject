
name := "akka-stream-scala"

version := "1.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.1"
)

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-http
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.10"
// https://mvnrepository.com/artifact/com.vk.api/sdk
libraryDependencies += "com.vk.api" % "sdk" % "0.5.6"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

fork in run := true