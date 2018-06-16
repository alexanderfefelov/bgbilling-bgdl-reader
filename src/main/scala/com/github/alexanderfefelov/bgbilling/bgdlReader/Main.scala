package com.github.alexanderfefelov.bgbilling.bgdlReader

import java.nio.charset.Charset

import better.files.File
import scodec.Codec
import scodec.bits.BitVector

object Main extends App {

  implicit val charset: Charset = Charset.forName("UTF-8")

  Config.parser.parse(args, Config()) match {
    case Some(config) => justDoIt(config)
    case None =>
  }

  private def justDoIt(config: Config): Unit = {
    val bgdlFile = File(config.bgdlFile.getAbsolutePath)
    if (!bgdlFile.exists || !bgdlFile.isRegularFile || !bgdlFile.isReadable) {
      println(s"Error: $bgdlFile doesn't exist, or is not a regular file, or is not readable")
      return
    }
    println(s".bgdl file: $bgdlFile")

    val bgdlBits = BitVector(bgdlFile.loadBytes)
    val bgdlDecoded = Codec[BgdlFile].decode(bgdlBits).require.value

    println(bgdlDecoded.header)
    bgdlDecoded.params.foreach(println)
  }

}
