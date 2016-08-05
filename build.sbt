import PicklingBuild.enableQuasiquotesIn210

val commonSettings = Seq(
    organization := "io.surfkit",
    version := "0.4.1",
    normalizedName ~= { _.replace("scala-js", "scalajs") },
    homepage := Some(url("http://scala-js.org/")),
    licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause")),
    scalaVersion := "2.11.5",
    crossScalaVersions := Seq("2.10.4", "2.11.5"),
    scalacOptions ++= Seq(
        //"-deprecation", // need to use deprecated things to be compat with 2.10
        "-unchecked",
        "-feature",
        "-encoding", "utf8"
    )
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
      publish := {},
      publishLocal := {}
  )
  .aggregate(corejs, corejvm, js, playjson, tests)

lazy val core = crossProject.crossType(CrossType.Pure)
  .settings(commonSettings: _*)
  .settings(resolverSettings: _*)
  .settings(enableQuasiquotesIn210: _*)
  .settings(
    name := "Scala.js pickling core",
    libraryDependencies +=
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val corejvm = core.jvm
lazy val corejs = core.js

lazy val js = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(resolverSettings: _*)
  .settings(
    name := "Scala.js pickling"
  )
  .dependsOn(corejs)

lazy val playjson = project
  .settings(commonSettings: _*)
  .settings(resolverSettings: _*)
  .settings(
    name := "Scala.js pickling play-json",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.0"
  )
  .dependsOn(corejvm)

// tests must be in a separate project for the IDE not to choke on macros
lazy val tests = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(resolverSettings: _*)
  .settings(
    publish := {},
    publishLocal := {},
    name := "Scala.js pickling tests",
    libraryDependencies +=
      "com.lihaoyi" %%% "utest" % "0.3.0" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .dependsOn(js)

lazy val resolverSettings = Seq(
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  resolvers += "NextWave Repo" at "http://maxdevmaster.cloudapp.net:4343/artifactory/nxtwv-maven/",
  publishTo := Some("NextWave Repo" at "http://maxdevmaster.cloudapp.net:4343/artifactory/nxtwv-maven/")
)
