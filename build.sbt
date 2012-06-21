name := "Player Simulator"

version := "1.0"

scalaVersion := "2.9.2"

seq(lwjglSettings: _*)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"

initialCommands := """
import com.github.seanparsons.playersimulator._
import scalaz._
import Scalaz._
"""
