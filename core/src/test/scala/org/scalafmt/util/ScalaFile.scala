package org.scalafmt.util

import java.io.File

case class ScalaFile(filename: String, projectUrl: String, commit: String) {

  def rawUrl = {
    val raw = projectUrl.replace("github.com", "raw.githubusercontent.com")
    s"$raw/$commit$filename"
  }

  def read: String = {
    val toRead = new File(FilesUtil.getFile("target", "repos", repo), filename)
    FilesUtil.readFile(toRead)
  }

  def githubUrl = s"$projectUrl/blob/$commit$filename"

  def userRepo = projectUrl.stripPrefix("https://github.com/")
  def repo = userRepo.split("/")(1)
  def user = userRepo.split("/")(0)

  override def toString: String = s"""ScalaFile(
                                     |    project: $user
                                     |    github: $githubUrl
                                     |    raw: $rawUrl
                                     |)""".stripMargin
}

object ScalaFile {
  def includeFile(filename: String): Boolean = {
    !Seq(
        // This fella seems to make the scalac parser hang (???)
        "target/repos/scala/test/files/neg/t5510.scala",
        // Unicode escapes in weird places
        "target/repos/scala/test/files/neg/t8015-ffb.scala",
        "target/repos/scala/test/files/pos/t389.scala",
        "target/repos/scala/test/files/run/literals.scala",
        "target/repos/scala/test/files/run/t3835.scala",
        // Scalac parser seems to accept this, though it blows up later
        "target/repos/scala/test/files/neg/t8266-invalid-interp.scala",
        "target/repos/scala/test/disabled/",
        "target/repos/scala/test/files/neg/",
        // trailing . after number
        "target/repos/scala/test/files/presentation/infix-completion/src/Snippet.scala",
        // Unicode escapes in weird places
        "target/repos/sbt/main/settings/src/main/scala/sbt/std/InputWrapper.scala",
        // uses a package called `macro`
        "target/repos/sbt/sbt/src/sbt-test/source-dependencies/inherited-macros",
        "target/repos/sbt/sbt/src/sbt-test/source-dependencies/macro",
        "target/repos/lila/modules/lobby/src/main/SocketHandler.scala")
      .exists(filename.contains)
  }

  def getAll: Seq[ScalaFile] = {
    val repos = FilesUtil.getFile("target", "repos")
    println(System.getProperty("user.dir"))
    val files = Option(repos.listFiles()).getOrElse {
      throw new IllegalStateException(
          s"""${repos.getAbsolutePath} is not a directory:
             |* wget https://github.com/olafurpg/scalafmt/releases/download/v0.1.4/repos.tar.gz
             |* tar xvf repos.tar.gz
        |""".stripMargin)
    }

    files.flatMap { repo =>
      val repoPrefix = repo.getPath // + File.pathSeparator
      val commit = FilesUtil.readFile(new File(repo, "COMMIT")).trim
      val url = FilesUtil.readFile(new File(repo, "URL")).trim
      FilesUtil
        .listFiles(repo)
        .withFilter(_.endsWith(".scala"))
        .withFilter(includeFile)
        .map { sourceFile =>
          val filename = sourceFile.stripPrefix(repoPrefix)
          ScalaFile(filename.trim, url, commit)
        }
    }
  }

  def forall(scalaFile: ScalaFile => Unit): Unit = {}
}
