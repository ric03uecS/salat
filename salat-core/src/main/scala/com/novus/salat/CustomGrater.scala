package com.novus.salat

import com.mongodb.DBObject
import com.mongodb.casbah.commons.Implicits._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.json.ToJField
import com.novus.salat.transformers.CustomTransformer
import org.json4s.JsonAST.JObject
import org.json4s._

class CustomGrater[ModelObject <: AnyRef](clazz: Class[ModelObject],
                                          transformer: CustomTransformer[ModelObject, DBObject])(implicit ctx: Context) extends Grater[ModelObject](clazz)(ctx) {

  def asDBObject(o: ModelObject) = transformer.serialize(o)

  def asObject[A <% MongoDBObject](dbo: A) = transformer.deserialize(unwrapDBObj(dbo))

  def toMap(o: ModelObject) = transformer.serialize(o).toMap.asInstanceOf[Map[String, Any]]

  def fromMap(m: Map[String, Any]) = transformer.deserialize(m.asDBObject)

  def toJSON(o: ModelObject) = {
    val builder = List.newBuilder[JField]
    builder ++= ToJField.typeHint(clazz, ctx.typeHintStrategy.when == TypeHintFrequency.Always)
    transformer.serialize(o).foreach {
      case (k, v) => builder += ToJField(k, v)
    }
    JObject(builder.result())
  }

  def fromJSON(j: JObject) = transformer.deserialize(j.values.asDBObject)

  def iterateOut[T](o: ModelObject)(f: ((String, Any)) => T) = Iterator.empty
}
