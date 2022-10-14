
lazy val b = project
  .settings(
    scalaVersion := "2.13.10",
    resolvers += Resolver.url("repo", (baseDirectory.in(ThisBuild).value / "repo" / "").toURI.toURL)(
      Resolver.defaultIvyPatterns
    ),
    libraryDependencies += "com.org" %% "a" % "0.1.0"
  )
