name := "FaqConverter"

version := "0.1"

scalaVersion := "2.12.13"

lazy val Fs2Version = "2.5.3"
lazy val CatsVersion = "2.4.2"
lazy val CatsEffectVersion = "2.4.1"

libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % CatsVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    "co.fs2" %% "fs2-core" % Fs2Version,
    "co.fs2" %% "fs2-io" % Fs2Version,
    "com.monovore" %% "decline" % "1.3.0",
    "com.monovore" %% "decline-effect" % "1.3.0"
)
