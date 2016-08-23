import sbt.Keys._
import Settings.versions

lazy val router = (project in file("router"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(Revolver.settings: _*)
  .settings(
    name := "scalafiddle-router",
    version := Settings.version,
    scalaVersion := versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % versions.akka,
      "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
      "com.typesafe.akka" %% "akka-http-core" % versions.akka,
      "com.typesafe.akka" %% "akka-http-experimental" % versions.akka,
      "com.lihaoyi" %% "upickle" % versions.upickle,
      "ch.megard" %% "akka-http-cors" % "0.1.4",
      "ch.qos.logback" % "logback-classic" % "1.1.7"
    ),
    javaOptions in Revolver.reStart ++= Seq("-Xmx2g"),
      dockerfile in docker := {
      val appDir: File = stage.value
      val targetDir = "/app"

      new Dockerfile {
        from("java:8")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir)
        expose(8080)
      }
    },
    imageNames in docker := Seq(
      ImageName(
        namespace = Some("scalafiddle"),
        repository = "scalafiddle-router",
        tag = Some("latest")
      ),
      ImageName(
        namespace = Some("scalafiddle"),
        repository = "scalafiddle-router",
        tag = Some(version.value)
      )
    )

  )