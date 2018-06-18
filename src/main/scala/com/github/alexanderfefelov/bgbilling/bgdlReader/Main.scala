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
    val f = File(config.bgdlFile.getAbsolutePath)
    if (!f.exists || !f.isRegularFile || !f.isReadable) {
      println(s"Error: $f doesn't exist, or is not a regular file, or is not readable")
      return
    }
    println(s".bgdl file: $f")

    val bgdlBits = BitVector(f.loadBytes)
    val bgdlFile = Codec[BgdlFile].decode(bgdlBits).require.value

    println(bgdlFile.header)
    bgdlFile.params.foreach(println)

  }

}
