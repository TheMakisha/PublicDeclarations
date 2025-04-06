package org.example

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import java.io.File
import kotlin.system.exitProcess

class KotlinFileAnalyzer {
  private val project = KotlinCoreEnvironment.createForProduction(
    Disposer.newDisposable(),
    CompilerConfiguration(),
    EnvironmentConfigFiles.JVM_CONFIG_FILES
  ).project;

  fun printPublicDeclarations(rootFile: File) {
    when {
      rootFile.isFile && rootFile.extension != "kt" -> {
        print("Error: Root file is not Kotlin file nor directory.")
        exitProcess(1)
      }
      rootFile.isDirectory -> {
        processDirectory(rootFile)
      }
      rootFile.isFile && rootFile.extension == "kt" -> {
        processKtFile(rootFile)
      }
    }
  }

  private fun createKtFileFromSource(fileName: String, sourceCode: String): KtFile {
    return PsiManager.getInstance(project)
      .findFile(LightVirtualFile(fileName, KotlinFileType.INSTANCE, sourceCode)) as KtFile;
  }

  private fun processDirectory(directory: File) {
    directory.listFiles()?.forEach { file ->
      when {
        file.isDirectory -> processDirectory(file)
        file.extension == "kt" -> processKtFile(file)
      }
    }
  }

  private fun processKtFile(file: File) {
    val ktFile = createKtFileFromSource(file.name, file.readText());

    ktFile.declarations.forEach { declaration ->
      printPublicDeclarations(declaration, 0)
    }
  }

  private fun printPublicDeclarations(declaration: KtDeclaration, nestingLevel: Int) {
    if (!declaration.isPublic) return
    if (declaration is KtFunction) {
      println("  ".repeat(nestingLevel) + "fun" + " ${declaration.name}()")
    }
    else {
      //We don't have to check whether the declaration is KtClass, KtEnum etc...
      //we can simply just get the keyword of the public declaration and print it with its name and
      //avoid expensive switch statement for each declaration
      //If the class is enum, abstract, sealed etc... it will simply print out class since all of them are
      //instances of KtClass
      if (declaration.node.findChildByType(KtTokens.KEYWORDS) != null) {
        println("  ".repeat(nestingLevel) + declaration.node.findChildByType(KtTokens.KEYWORDS)?.text + " ${declaration.name}")
      }
    }

    when (declaration) {
      is KtClassOrObject -> {
        print("  ".repeat(nestingLevel) + "{");
        println();
        declaration.declarations.forEach { dec ->
          printPublicDeclarations(dec, nestingLevel + 1)
        }
        println("  ".repeat(nestingLevel) + "}")
      }
    }
  }
}