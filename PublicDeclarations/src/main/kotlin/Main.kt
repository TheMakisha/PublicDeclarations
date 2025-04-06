package org.example

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

  if (args.isEmpty()) {
    println("Project path isn't specified")
    exitProcess(1)
  }

  val path = args[0];
  val file = File(path)
  if (!file.exists()) {
    println("Provided project path does not exist")
    exitProcess(1)
  }

  val analyzer = KotlinFileAnalyzer()
  analyzer.printPublicDeclarations(file)
}
