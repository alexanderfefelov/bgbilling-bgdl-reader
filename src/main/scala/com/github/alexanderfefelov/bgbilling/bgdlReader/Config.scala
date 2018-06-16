package com.github.alexanderfefelov.bgbilling.bgdlReader

import java.io.File

import scopt._
import version._

case class Config(
  bgdlFile: File = new File("."),
)

object Config {

  val parser: OptionParser[Config] = new OptionParser[Config](s"java -jar bbr.jar") {
    head(s"${BuildInfo.name} v. ${BuildInfo.version}",
      """
        |
        |Copyright (C) 2018 Alexander Fefelov <https://github.com/alexanderfefelov>
        |This program comes with ABSOLUTELY NO WARRANTY; see LICENSE file for details.
        |This is free software, and you are welcome to redistribute it under certain conditions; see LICENSE file for details.
      """.stripMargin)

    opt[File]('b', "bgdl-file")
      .valueName("<path>")
      .required()
      .action((x, c) => c.copy(bgdlFile = x))
      .text("Specifies path to .bgdl file. This parameter is required")

    help("help").text("Prints this usage text")
  }

}
