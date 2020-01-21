package tests.feature

import tests.BaseCompletionLspSuite
import scala.meta.internal.metals.BuildInfo
import scala.concurrent.Future

class DefinitionCrossLspSuite
    extends BaseCompletionLspSuite("definition-cross") {

  if (super.isValidScalaVersionForEnv(BuildInfo.scala211)) {
    test("2.11") {
      basicDefinitionTest(BuildInfo.scala211)
    }
  }

  if (super.isValidScalaVersionForEnv(BuildInfo.scala213)) {
    test("2.13") {
      basicDefinitionTest(BuildInfo.scala213)
    }
  }

  def basicDefinitionTest(scalaVersion: String): Future[Unit] = {
    cleanDatabase()
    for {
      _ <- server.initialize(
        s"""
           |/metals.json
           |{
           |  "a": {
           |    "scalaVersion": "${scalaVersion}"
           |  }
           |}
           |/a/src/main/scala/a/Main.scala
           |object Main {
           |  println("hello!")
           |}
           |""".stripMargin
      )
      _ = client.messageRequests.clear()
      _ <- server.didOpen("a/src/main/scala/a/Main.scala")
      _ = server.workspaceDefinitions // trigger definition
      _ <- server.didOpen("scala/Predef.scala")
      _ = assertNoDiff(
        client.workspaceMessageRequests,
        ""
      )
      _ = assertNoDiff(client.workspaceDiagnostics, "")
    } yield ()
  }
}
