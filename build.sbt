
inThisBuild(List(
  organization := "io.get-coursier",
  homepage := Some(url("https://github.com/coursier/sbt-cs-publish")),
  licenses := Seq("Apache 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
  developers := List(
    Developer(
      "alexarchambault",
      "Alexandre Archambault",
      "",
      url("https://github.com/alexarchambault")
    )
  )
))

val scala212 = "2.12.14"
val sbt10Version = "1.0.2"

lazy val shared = Seq(
  scalaVersion := scala212
)

lazy val `sbt-cs-publish` = project
  .enablePlugins(ScriptedPlugin)
  .settings(
    shared,
    // https://github.com/sbt/sbt/issues/5049#issuecomment-528960415
    dependencyOverrides := "org.scala-sbt" % "sbt" % "1.2.8" :: Nil,
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      "-Dplugin.name=" + name.value,
      "-Dplugin.version=" + version.value,
      "-Dsbttest.base=" + (sourceDirectory.value / "sbt-test").getAbsolutePath
    ),
    scriptedBufferLog := false,
    sbtPlugin := true,
    sbtVersion.in(pluginCrossBuild) := sbt10Version
  )

lazy val `sbt-cs-publish-root` = project
  .in(file("."))
  .aggregate(`sbt-cs-publish`)
  .settings(
    shared,
    skip.in(publish) := true
  )
