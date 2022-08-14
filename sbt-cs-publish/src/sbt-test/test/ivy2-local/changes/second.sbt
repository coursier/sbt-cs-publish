
lazy val b = project
  .settings(
    ivyPaths := ivyPaths.value.withIvyHome(
      baseDirectory.in(ThisBuild).value / "ivy-home"
    ),
    scalaVersion := "2.13.8",
    libraryDependencies += "com.org" %% "a" % "0.1.0"
  )
