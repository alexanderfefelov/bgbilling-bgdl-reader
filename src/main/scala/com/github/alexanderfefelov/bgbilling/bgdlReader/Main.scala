package com.github.alexanderfefelov.bgbilling.bgdlReader

import java.net.InetAddress
import java.nio.charset.Charset

import better.files.File
import org.araqne.netflow.v9._
import scodec.Codec
import scodec.bits.BitVector

object Main extends App {

  implicit val charset: Charset = Charset.forName("UTF-8")

  Config.parser.parse(args, Config()) match {
    case Some(config) => justDoIt(config)
    case None =>
  }

  private def justDoIt(config: Config): Unit = {
    val bf = File(config.bgdlFile.getAbsolutePath)
    if (!bf.exists || !bf.isRegularFile || !bf.isReadable) {
      println(s"Error: $bf doesn't exist, or is not a regular file, or is not readable")
      return
    }
    println(s".bgdl file: $bf, ${bf.size} byte(s)")

    val bgdlBits = BitVector(bf.loadBytes)
    val bgdlFile = Codec[BgdlFile].decode(bgdlBits).require.value

    if (!bgdlFile.distributed) {
      println("Error: Unable to process non-distributed DataLog file")
      return
    }

    val df = File(config.bgdlFile.getAbsolutePath.replaceAll("""\.bgdl$""", ".data"))
    if (!df.exists || !df.isRegularFile || !df.isReadable) {
      println(s"Error: $df doesn't exist, or is not a regular file, or is not readable")
      return
    }
    println(s".data file: $df, ${df.size} byte(s)")

    bgdlFile.ipDataLogType.typ match {
      case IP_DATALOG_TYPE_NETFLOW =>
        processNetFlow()
      case IP_DATALOG_TYPE_NETFLOW_V9 =>
        processNetFlowV9()
      case _ =>
        println("Error: Unsupported IP DataLog type")
    }
  }

  private def processNetFlow(): Unit = {

  }

  private def processNetFlowV9(): Unit = {
    val templateCache = new NetFlowV9TemplateCache(InetAddress.getLocalHost, -1)
  }

}
