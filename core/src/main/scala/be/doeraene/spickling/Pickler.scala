package be.doeraene.spickling

import java.util.{Date => JDate, UUID}
import scala.collection.immutable.Map.{Map1, Map2, Map3, Map4}
import scala.collection.immutable.HashMap.HashTrieMap
import scala.collection.immutable.Set.{Set1, Set2, Set3, Set4}
import scala.collection.immutable.HashSet.HashTrieSet

trait Pickler[A] {
  type Picklee = A

  def pickle[P](obj: Picklee)(implicit registry: PicklerRegistry,
      builder: PBuilder[P]): P
}

object Pickler extends PicklerMaterializers {
  implicit object BooleanPickler extends Pickler[Boolean] {
    def pickle[P](x: Boolean)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeBoolean(x)
  }

  implicit object CharPickler extends Pickler[Char] {
    def pickle[P](x: Char)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeString(x.toString)
  }

  implicit object BytePickler extends Pickler[Byte] {
    def pickle[P](x: Byte)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeNumber(x)
  }

  implicit object ShortPickler extends Pickler[Short] {
    def pickle[P](x: Short)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeNumber(x)
  }

  implicit object IntPickler extends Pickler[Int] {
    def pickle[P](x: Int)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeNumber(x)
  }

  implicit object LongPickler extends Pickler[Long] {
    def pickle[P](x: Long)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = {
      builder.makeObject(
          ("l", builder.makeNumber(x.toInt & 0x3fffff)),
          ("m", builder.makeNumber((x >> 22).toInt & 0x3fffff)),
          ("h", builder.makeNumber((x >> 44).toInt)))
    }
  }

  implicit object FloatPickler extends Pickler[Float] {
    def pickle[P](x: Float)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeNumber(x)
  }

  implicit object DoublePickler extends Pickler[Double] {
    def pickle[P](x: Double)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeNumber(x)
  }

  implicit object StringPickler extends Pickler[String] {
    def pickle[P](x: String)(implicit registry: PicklerRegistry,
        builder: PBuilder[P]): P = builder.makeString(x)
  }

  /*
   * UUID
   */

  implicit object UUIDPickler extends Pickler[UUID] {
    def pickle[P](x: UUID)(implicit registry: PicklerRegistry,
                             builder: PBuilder[P]): P = builder.makeString(x.toString)
  }


  /*
   * Vector
   */

  trait BaseVectorPickler[A <: Vector[Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map{
        case x: java.lang.Integer => IntPickler.pickle(x)
        case x: java.lang.Double => DoublePickler.pickle(x)
        case x: java.lang.Float => FloatPickler.pickle(x)
        case x: java.lang.Character => CharPickler.pickle(x)
        case x: java.lang.Long => LongPickler.pickle(x)
        case x: java.lang.Boolean => BooleanPickler.pickle(x)
        case v => registry.pickle(v)
      }.toSeq: _*)
    }
  }

  implicit object VectorPickler extends BaseVectorPickler[Vector[Any]]

  // Notes on Array[T]
  //http://stackoverflow.com/questions/6867131/getclass-method-java-with-array-types
  trait BaseArrayPickler[A <: Array[Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map{
        case x: java.lang.Integer => DoublePickler.pickle(x.toDouble)
        case x: java.lang.Double => DoublePickler.pickle(x)
        case x: java.lang.Float => FloatPickler.pickle(x)
        case x: java.lang.Character => CharPickler.pickle(x)
        case x: java.lang.Long => LongPickler.pickle(x)
        case x: java.lang.Boolean => BooleanPickler.pickle(x)
        case v => registry.pickle(v)
      }.toSeq: _*)
    }
  }

  implicit object ArrayPickler extends BaseArrayPickler[Array[Any]]


  // TODO: not sure of a way around having to define these yet
  implicit object DoubleArrayPickler extends Pickler[Array[Double]] {
    def pickle[P](x: Array[Double])(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map(v => DoublePickler.pickle(v)).toSeq: _*)
    }
  }

  /*implicit object DoubleArrayPickler extends Pickler[Array[Double]] {
    def pickle[P](x: Array[Double])(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map(v => DoublePickler.pickle(v)).toSeq: _*)
    }
  }*/


  /*
   * JDate
   */

  implicit object JDatePickler extends Pickler[JDate] {
    def pickle[P](x: JDate)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P =
      Pickler.LongPickler.pickle(x.getTime())(registry, builder)
  }

  /*
   * Map
   */

  trait BaseMapPickler[A <: Map[Any,Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map { case (key, value) =>
        builder.makeObject(
          ("k", registry.pickle(key)),
          ("v", registry.pickle(value))
        )
      }.toSeq: _*)
    }
  }

  implicit object MapPickler extends BaseMapPickler[Map[Any,Any]]
  implicit object Map1ickler extends BaseMapPickler[Map1[Any,Any]]
  implicit object Map2ickler extends BaseMapPickler[Map2[Any,Any]]
  implicit object Map3ickler extends BaseMapPickler[Map3[Any,Any]]
  implicit object Map4ickler extends BaseMapPickler[Map4[Any,Any]]
  implicit object HashTrieMapPickler extends BaseMapPickler[HashTrieMap[Any,Any]]

  /*
   * Set
   */

  trait BaseSetPickler[A <: Set[Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P =
      builder.makeArray(x.map { v => registry.pickle(v) }.toSeq: _*)
  }

  implicit object SetPickler extends BaseSetPickler[Set[Any]]
  implicit object Set1Pickler extends BaseSetPickler[Set1[Any]]
  implicit object Set2Pickler extends BaseSetPickler[Set2[Any]]
  implicit object Set3Pickler extends BaseSetPickler[Set3[Any]]
  implicit object Set4Pickler extends BaseSetPickler[Set4[Any]]
  implicit object HashTrieSetPickler extends BaseSetPickler[HashTrieSet[Any]]
}
