package sbtcspublish

import java.net.URI

import sbt.internal.SessionSettings
import sbt.{BuiltinCommands, GlobalScope, Project, Reference, Scope, Select, Setting, State, Zero}

// Adapted from the sbt-structure plugin.
// Allows to load an `AutoPlugin` via the `apply` command of sbt, passing it the JAR of the plugin and the plugin class name.
// The plugin should have no other dependencies than sbt for that to work.
trait ApplyState extends (State => State) {

  def projectSettings: Seq[Setting[_]]
  def globalSettings: Seq[Setting[_]]


  // from https://github.com/JetBrains/sbt-structure/blob/16574c8d17afcc99de9aa6d73ac0e5349c9a32a4/extractor/src/main/scala-sbt-0.13-1.0/org/jetbrains/sbt/CreateTasks.scala

  def apply(state: State): State =
    applySettings(state, globalSettings, projectSettings)

  private def applySettings(state: State, globalSettings: Seq[Setting[_]], projectSettings: Seq[Setting[_]]): State = {
    val extracted = Project.extract(state)
    import extracted.{structure => extractedStructure, _}
    val transformedGlobalSettings = Project.transform(_ => GlobalScope, globalSettings)
    val transformedProjectSettings = extractedStructure.allProjectRefs.flatMap { projectRef =>
      transformSettings(projectScope(projectRef), projectRef.build, rootProject, projectSettings)
    }
    reapply(extracted.session.appendRaw(transformedGlobalSettings ++ transformedProjectSettings), state)
  }

  private def transformSettings(thisScope: Scope, uri: URI, rootProject: URI => String, settings: Seq[Setting[_]]): Seq[Setting[_]] =
    Project.transform(Scope.resolveScope(thisScope, uri, rootProject), settings)

  private def reapply(session: SessionSettings, s: State): State =
    BuiltinCommands.reapply(session, Project.structure(s), s)

  private def projectScope(project: Reference): Scope = Scope(Select(project), Zero, Zero, Zero)

}
