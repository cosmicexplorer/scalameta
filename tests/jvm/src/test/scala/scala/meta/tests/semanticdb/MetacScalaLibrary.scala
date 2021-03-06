package scala.meta.tests.semanticdb

import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import scala.meta.cli._
import scala.meta.tests.BuildInfo
import org.langmeta.internal.io.FileIO
import org.langmeta.io.AbsolutePath

// Compile all of scala-library with metac and report any semanticdb errors.
object MetacScalaLibrary {
  def main(args: Array[String]): Unit = {
    val library = Paths
      .get("target")
      .resolve("scala-library")
      .resolve(s"scala-${BuildInfo.scalaVersion}")
      .resolve("src")
      .resolve("library")
      .toAbsolutePath
    assert(Files.isDirectory(library), s"$library is not a directory! Run `sbt ci-metac`")
    val classpath = this.getClass.getClassLoader
      .asInstanceOf[URLClassLoader]
      .getURLs
      .map(_.getPath)
      .filter(_.contains("scala-library"))
      .mkString(File.pathSeparator)
    val files = FileIO
      .listAllFilesRecursively(AbsolutePath(library))
      .map(_.toString)
      .filter(_.endsWith("scala"))
    val out = Files.createTempDirectory("metac")
    println(s"Compiling ${files.length} sources from scala-library...")
    val args = Array(
      "-d",
      out.toString,
      "-classpath",
      classpath,
      "-P:semanticdb:failures:error"
    ) ++ files
    val code = Metac.process(args, System.out, System.err)
    println(out)
    sys.exit(code)
  }
}
