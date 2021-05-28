
lazy val b = project
  .settings(
    scalaVersion := "2.12.14",
    resolvers += "repo" at (baseDirectory.in(ThisBuild).value / "repo").toURI.toASCIIString,
    libraryDependencies += "org" %% "a" % "0.1.0"
  )
