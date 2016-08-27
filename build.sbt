import sbt.Keys._
import Settings.versions

val commonSettings = Seq(
  scalacOptions := Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  ),
  scalaVersion := versions.scala,
  scalacOptions ++= Settings.scalacOptions,
  version := Settings.version
)

lazy val client = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % versions.dom,
      "com.lihaoyi" %%% "scalatags" % versions.scalatags,
      "com.lihaoyi" %%% "upickle" % versions.upickle,
      "com.github.marklister" %%% "base64" % "0.2.2",
      "org.scala-lang.modules" %% "scala-async" % versions.async % "provided"
    ),
    // rename output always to -opt.js
    artifactPath in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value /
      ((moduleName in fastOptJS).value + "-opt.js")),
    relativeSourceMaps := true
  )

lazy val router = (project in file("router"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(Revolver.settings: _*)
  .settings(commonSettings)
  .settings(
    name := "scalafiddle-router",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % versions.akka,
      "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
      "com.typesafe.akka" %% "akka-http-core" % versions.akka,
      "com.typesafe.akka" %% "akka-http-experimental" % versions.akka,
      "com.lihaoyi" %% "scalatags" % versions.scalatags,
      "org.webjars" % "ace" % versions.ace,
      "org.webjars" % "normalize.css" % "2.1.3",
      "com.lihaoyi" %% "upickle" % versions.upickle,
      "ch.megard" %% "akka-http-cors" % "0.1.4",
      "ch.qos.logback" % "logback-classic" % "1.1.7"
    ),
    javaOptions in Revolver.reStart ++= Seq("-Xmx1g"),
    scriptClasspath := Seq("../config/") ++ scriptClasspath.value,
    resourceGenerators in Compile += Def.task {
      // store build version in a property file
      val file = (resourceManaged in Compile).value / "version.properties"
      val contents =
        s"""
           |version=${version.value}
           |scalaVersion=${scalaVersion.value}
           |scalaJSVersion=$scalaJSVersion
           |aceVersion=${versions.ace}
           |""".stripMargin
      IO.write(file, contents)
      Seq(file)
    }.taskValue,
    (resources in Compile) ++= {
      Seq((fullOptJS in(client, Compile)).value.data)
    },
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

lazy val root = project.in(file("."))
  .aggregate(client, router)
  .settings(
    (resources in(router, Compile)) ++= {
      Seq((fastOptJS in(client, Compile)).value.data)
    }
  )
