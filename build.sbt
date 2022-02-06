ThisBuild / version := "2.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.0.0-RC2"

lazy val root = (project in file("."))
  .settings(
    name := "brkldbis",
    idePackagePrefix := Some("com.pmkitten.brkldbis"),
    libraryDependencies += "com.sleepycat" % "je" % "18.3.12"
  )

