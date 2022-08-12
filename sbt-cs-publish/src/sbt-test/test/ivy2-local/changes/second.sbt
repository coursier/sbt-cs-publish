
lazy val b = project
  .settings(
    ivyPaths := ivyPaths.value.withIvyHome(
      baseDirectory.in(ThisBuild).value / "ivy-home"
    ),
    scalaVersion := "2.12.16",
    libraryDependencies += "com.org" %% "a" % "0.1.0"
  )
