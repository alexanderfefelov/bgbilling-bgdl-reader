package com.github.alexanderfefelov.bgbilling.bgdlReader

import scodec._
import codecs._
import scodec.bits._

case class BgdlFile(header: Header, params: Vector[Param]) {

  def finished: Boolean = {
    params.find(_.typ.typ == PARAM_TYPE_FINISHED) match {
      case Some(param) => param.value(0) > 0
    }
  }

  def buffer: Buffer = {
    params.find(_.typ.typ == PARAM_TYPE_BUFFER) match {
      case Some(param) =>
        // First byte: type
        // Four bytes: chunk size
        Buffer(
          param.value(0),
          param.value.drop(1).take(4).toInt()
        )
    }
  }

  def compression: Compression = {
    params.find(_.typ.typ == PARAM_TYPE_COMPRESSION) match {
      case Some(param) =>
        // First byte: type
        // Second byte: level
        // Third byte: strategy
        Compression(
          param.value(0),
          param.value(1),
          param.value(2)
        )
    }
  }

  def distributed: Boolean = {
    params.find(_.typ.typ == PARAM_TYPE_DISTRIBUTED) match {
      case Some(param) => param.value(0) > 0
    }
  }

  def streaming: Boolean = {
    params.find(_.typ.typ == PARAM_TYPE_STREAMING) match {
      case Some(param) => param.value(0) > 0
    }
  }

  def ipDataLogType: IpDataLogType = {
    params.find(_.typ.typ == PARAM_TYPE_IP_DATALOG_TYPE) match {
      case Some(param) =>
        // First byte: type
        // Second byte: subtype
        // Third byte: subtype version
        IpDataLogType(
          param.value(0),
          param.value(1),
          param.value(2)
        )
    }
  }

  def netFlowV9Template: Option[ByteVector] = {
    params.find(_.typ.typ == PARAM_TYPE_NETFLOW_V9_TEMPLATE) match {
      case Some(param) => Some(param.value)
      case _ => None
    }
  }

}

case class Header(magic: String, version: Long, typ: Long) {

  require(magic == DATALOG_MAGIC, "magic not found")
  require(version == 4, "unsupported DataLog version")
  require(typ == DATALOG_TYPE_IP, "unsupported DataLog type")

}

case class Param(typ: ParamType, value: ByteVector)

case class ParamType(typ: Long) {

  override def toString: String = typ match {
    case PARAM_TYPE_FINISHED => "Finished"
    case PARAM_TYPE_BUFFER => "Buffer"
    case PARAM_TYPE_COMPRESSION => "Compression"
    case PARAM_TYPE_DISTRIBUTED => "Distributed"
    case PARAM_TYPE_STREAMING => "Streaming"
    case PARAM_TYPE_IP_DATALOG_TYPE => "IP DataLog Type"
    case PARAM_TYPE_NETFLOW_V9_TEMPLATE => "NetFlow v9 Template"
    case _ => "UNKNOWN"
  }

}

case class Buffer(typ: Byte, chunkSize: Int)
case class Compression(typ: Byte, level: Byte, strategy: Byte)
case class IpDataLogType(typ: Byte, subType: Byte, subTypeVersion: Byte)

object BgdlFile {

  implicit val paramType: Codec[ParamType] = uint32.xmap(ParamType.apply, _.typ)

  implicit val bgdlParam: Codec[Param] = (
    ("typ"   | paramType) ::
    ("value" | variableSizeBytesLong(uint32, bytes))
  ).as[Param]

  implicit val bgdlHeader: Codec[Header] = (
    ("magic"   | fixedSizeBytes(DATALOG_MAGIC.length, ascii)) ::
    ("version" | uint32) ::
    ("typ"     | uint32)
  ).as[Header]

  implicit val bgdlFile: Codec[BgdlFile] = (
    ("header" | bgdlHeader) ::
    ("params" | variableSizeBytesLong(uint32, vector(bgdlParam)))
  ).as[BgdlFile]

}
