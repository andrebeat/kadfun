name := "kadfun"

version := "0.1"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "com.typesafe.akka"      %% "akka-actor" % "2.3.4")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature")
