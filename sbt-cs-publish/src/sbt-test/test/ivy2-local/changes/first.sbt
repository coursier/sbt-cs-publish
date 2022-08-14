
lazy val a = project
  .settings(
    ivyPaths := ivyPaths.value.withIvyHome(
      baseDirectory.in(ThisBuild).value / "ivy-home"
    ),
    organization := "com.org",
    version := "0.1.0",
    scalaVersion := "2.13.8"
  )
