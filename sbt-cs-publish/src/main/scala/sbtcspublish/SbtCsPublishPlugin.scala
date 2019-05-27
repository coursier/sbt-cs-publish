package sbtcspublish

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}

import sbt.{AutoPlugin, Def, ThisBuild, file, fileToRichFile, inputKey, taskKey}
import sbt.complete.DefaultParsers._
import sbt.Keys.{baseDirectory, ivyPaths, makeIvyXml, packagedArtifacts, projectID, scalaModuleInfo}
import sbt.librarymanagement.{Artifact, CrossVersion}

object SbtCsPublishPlugin extends AutoPlugin with ApplyState {

  object autoImport {
    val csPublishLocal = taskKey[Unit]("")
    val csPublish = inputKey[Unit]("")
  }

  import autoImport._

  override def trigger = allRequirements

  override def requires = sbt.plugins.JvmPlugin

  private def pubLocal: Def.Initialize[sbt.Task[Unit]] =
    Def.taskDyn {
      def defaultIvyHome = file(sys.props("user.home")) / ".ivy2"
      val ivyHome = ivyPaths.value.ivyHome.getOrElse(defaultIvyHome)
      val ivy2Local = ivyHome / "local"
      val arg = s"ivy:${ivy2Local.getCanonicalPath}"
      csPublish.toTask(arg)
    }

  private def pub: Def.Initialize[sbt.InputTask[Unit]] =
    Def.inputTaskDyn {
      val repo =
        Some((OptSpace ~> StringBasic).parsed.trim)
          .filter(_.nonEmpty)
          .getOrElse("")

      val (publishIvy, repo0) =
        if (repo.startsWith("ivy:"))
          (true, repo.stripPrefix("ivy:"))
        else
          (false, repo)

      val projId = projectID.value

      val baseDir = baseDirectory.in(ThisBuild).value

      val repoPath = {
        val p = Paths.get(repo0)
        if (p.isAbsolute) p
        else baseDir.toPath.resolve(p)
      }

      val scalaModInfo = scalaModuleInfo.value
      val cross = CrossVersion(projId, scalaModInfo)
      // Getting "Illegal dynamic reference: Function1" with a fold like
      //   val nme = cross.fold(projId.name)(_(projId.name))
      val nme = cross match {
        case None => projId.name
        case Some(f) => f(projId.name)
      }

      val baseArtifacts = packagedArtifacts.value.toVector.sortBy(_._2.getAbsolutePath).map {
        case (a, f) =>
          CrossVersion.substituteCross(a, cross) -> f
      }

      val ivyXmlTask =
        if (publishIvy && baseArtifacts.nonEmpty)
          Def.task {
            val f = makeIvyXml.value
            val a = Artifact("ivy")
              .withExtension("xml")
              .withType("ivy")
            Seq(a -> f)
          }
        else
          Def.task(Seq.empty[(Artifact, File)])

      Def.task {
        val allArtifacts = baseArtifacts ++ ivyXmlTask.value

        // System.err.println(s"${m.size} artifacts:")
        // for ((a, f) <- m.toVector.sortBy(_._2.getAbsolutePath))
        //   System.err.println(s"$a\n  $f")
        // System.err.println()

        val mavenDir = {
          val dir = projId.organization.split('.').filter(_.nonEmpty).toSeq ++ Seq(nme, projId.revision)
          repoPath.resolve(dir.mkString("/"))
        }

        val ivyDir = {
          val dir = Seq(projId.organization, nme) ++
            projId.extraAttributes.get("scalaVersion").toSeq.map("scala_" + _) ++
            projId.extraAttributes.get("sbtVersion").toSeq.map("sbt_" + _) ++
            Seq(projId.revision)
          repoPath.resolve(dir.mkString("/"))
        }

        for ((a, f) <- allArtifacts) {
          val dest =
            if (publishIvy)
              ivyDir.resolve(s"${a.`type`}s/${a.name}${a.classifier.fold("")("-" + _)}.${a.extension}")
            else
              mavenDir.resolve(s"${a.name}-${projId.revision}${a.classifier.fold("")("-" + _)}.${a.extension}")

          System.err.println(s"Writing $dest")
          Files.createDirectories(dest.getParent)
          Files.copy(
            f.toPath,
            dest,
            StandardCopyOption.REPLACE_EXISTING // should we?
          )
        }
      }
    }

  override def projectSettings = Seq[sbt.Setting[_]](
    csPublishLocal := pubLocal.value,
    csPublish := pub.evaluated
  )

}
