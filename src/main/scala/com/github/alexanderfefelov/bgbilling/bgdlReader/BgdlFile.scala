package com.github.alexanderfefelov.bgbilling.bgdlReader

import scodec._
import codecs._
import scodec.bits._

case class BgdlFile(header: BgdlHeader, params: Vector[BgdlParam])

case class BgdlHeader(magic: String, version: Long, typ: Long) {

  require(magic == "BGDL", "magic not found")
  require(version == 4, "unsupported DataLog version")
  require(typ == 1, "unsupported DataLog type")

}

case class BgdlParam(typ: ParamType, value: ByteVector)

case class ParamType(typ: Long) {

  override def toString: String = typ match {
    case 3 => "Finished"

    // First byte: 0 - none, 1 - chunked
    // Four bytes: chunk size
    case 4 => "Buffer"

    // First byte: 1 - zlib, 2 - gzip, 3 - xz
    // Second byte: level
    // Third byte: strategy
    case 5 => "Compression"

    case 6 => "Distributed"
    case 7 => "Streaming"

    // First byte: 0 - raw, 1 - NetFlow, 2 - sFlow, 3 - SNMP, 4 - NetFlow v9
    // Second byte: subtype
    // Third byte: subtype version
    case 100 => "IP DataLog Type"

    case 110 => "NetFlow v9 Template"
    case _ => "UNKNOWN"
  }

}

object BgdlFile {

  implicit val paramType: Codec[ParamType] = uint32.xmap(ParamType.apply, _.typ)

  implicit val bgdlParam: Codec[BgdlParam] = (
    paramType ::
    variableSizeBytesLong(uint32, bytes)
  ).as[BgdlParam]

  implicit val bgdlHeader: Codec[BgdlHeader] = (
    fixedSizeBytes(4, ascii) ::
    uint32 ::
    uint32
  ).as[BgdlHeader]

  implicit val bgdlFile: Codec[BgdlFile] = (
    bgdlHeader ::
    variableSizeBytesLong(uint32, vector(bgdlParam))
  ).as[BgdlFile]

}
