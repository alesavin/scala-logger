package com.emarsys.logger.internal

import shapeless._
import shapeless.labelled.FieldType

trait ToMapRec[L <: HList] { def apply(l: L): Map[String, Any] }

trait LowPriorityToMapRec {
  implicit def headNotCaseClassToMap[K <: Symbol, V, T <: HList](
      implicit
      wit: Witness.Aux[K],
      toMap: Lazy[ToMapRec[T]]): ToMapRec[FieldType[K, V] :: T] =
    (labelledList: FieldType[K, V] :: T) => {
      val fieldName     = wit.value.name
      val fieldValue    = labelledList.head
      val entry         = fieldName -> fieldValue
      val rest          = labelledList.tail
      val restConverted = toMap.value(rest)

      restConverted + entry
    }
}

object ToMapRec extends LowPriorityToMapRec {
  implicit val hnilToMap: ToMapRec[HNil] = (_: HNil) => {
    Map.empty
  }

  implicit def hconsToMapRec0[K <: Symbol, V, R <: HList, T <: HList](
      implicit
      wit: Witness.Aux[K],
      gen: LabelledGeneric.Aux[V, R],
      toMapR: Lazy[ToMapRec[R]],
      toMapT: Lazy[ToMapRec[T]]): ToMapRec[FieldType[K, V] :: T] = (labelledList: FieldType[K, V] :: T) => {
    val fieldValueWithNestedList = labelledList.head
    val valueConverted           = toMapR.value(gen.to(fieldValueWithNestedList))
    val fieldName                = wit.value.name
    val entry                    = fieldName -> valueConverted
    val rest                     = labelledList.tail
    val restConverted            = toMapT.value(rest)

    restConverted + entry
  }

  def toMap[A, L <: HList](a: A)(implicit gen: LabelledGeneric.Aux[A, L], toMap: Lazy[ToMapRec[L]]): Map[String, Any] = {
    val genericRepresentation = gen.to(a)
    toMap.value(genericRepresentation)
  }

}
