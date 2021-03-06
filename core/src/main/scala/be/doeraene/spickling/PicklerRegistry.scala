package be.doeraene.spickling

import java.util.UUID

import scala.reflect.ClassTag
import scala.collection.mutable

import scala.collection.immutable.Map.{Map1, Map2, Map3, Map4}
import scala.collection.immutable.HashMap.HashTrieMap
import scala.collection.immutable.Set.{Set1, Set2, Set3, Set4}
import scala.collection.immutable.HashSet.HashTrieSet

object PicklerRegistry extends BasePicklerRegistry {
  class SingletonFullName[A](val name: String)

  object SingletonFullName extends PicklerMaterializers
}

trait PicklerRegistry {
  def pickle[P](value: Any)(implicit builder: PBuilder[P],
      registry: PicklerRegistry = this): P
  def unpickle[P](pickle: P)(implicit reader: PReader[P],
      registry: PicklerRegistry = this): Any

  def contains(value: Any):Boolean
}

class BasePicklerRegistry extends PicklerRegistry {
  import PicklerRegistry._

  private val picklers = new mutable.HashMap[String, Pickler[_]]
  private val unpicklers = new mutable.HashMap[String, Unpickler[_]]
  private val singletons = new mutable.HashMap[Any, String]
  private val singletonsRev = new mutable.HashMap[String, Any]

  registerBuiltinPicklers()

  private def registerInternal(clazz: Class[_], pickler: Pickler[_],
      unpickler: Unpickler[_]): Unit = {
    picklers(clazz.getName) = pickler
    unpicklers(clazz.getName) = unpickler
  }

  def register[A : ClassTag](pickler: Pickler[A],
      unpickler: Unpickler[A]): Unit = {
    registerInternal(implicitly[ClassTag[A]].runtimeClass, pickler, unpickler)
  }

  def register[A : ClassTag](implicit pickler: Pickler[A],
      unpickler: Unpickler[A]): Unit = {
    register(pickler, unpickler)
  }

  def register[A <: Singleton](obj: A)(implicit name: SingletonFullName[A]): Unit = {
    singletons(obj) = name.name
    singletonsRev(name.name) = obj
  }

  def contains(value: Any):Boolean = {
    val className = value.getClass.getName match {
      case "java.lang.Byte" | "java.lang.Short" => "java.lang.Integer"
      case "java.lang.Float"                    => "java.lang.Double"
      case x if x.startsWith("[L")              => "[Ljava.lang.Object;"
      case name                                 => name
    }
    picklers.contains(className)
  }

  def pickle[P](value: Any)(implicit builder: PBuilder[P], registry: PicklerRegistry): P = {
    if (value == null) {
      builder.makeNull()
    } else {
      //println(s"value: ${value}")
      singletons.get(value) match {
        case Some(name) => builder.makeObject(("s", builder.makeString(name)))
        case _ =>
          val className = value.getClass.getName match {
            case "java.lang.Byte" | "java.lang.Short" => "java.lang.Integer"
            case "java.lang.Float"                    => "java.lang.Double"
            case x if x.startsWith("[L")               => "[Ljava.lang.Object;"
            case name                                 => name
          }
          //println(s"Lookup for className: ${className}")
          val pickler = picklers(className)
          val pickledValue = pickler.pickle[P](value.asInstanceOf[pickler.Picklee])
          builder.makeObject(
              ("t", builder.makeString(className)),
              ("v", pickledValue))
      }
    }
  }

  def unpickle[P](pickle: P)(implicit reader: PReader[P], registry: PicklerRegistry): Any = {
    if (reader.isNull(pickle)) {
      null
    } else {
      val s = reader.readObjectField(pickle, "s")
      if (!reader.isUndefined(s)) {
        singletonsRev(reader.readString(s))
      } else {
        val className = reader.readString(reader.readObjectField(pickle, "t"))
        val unpickler = unpicklers(className)
        unpickler.unpickle[P](reader.readObjectField(pickle, "v"))
      }
    }
  }

  private def registerBuiltinPicklers(): Unit = {
    registerPrimitive[Boolean, java.lang.Boolean]
    registerPrimitive[Char, java.lang.Character]
    registerPrimitive[Byte, java.lang.Byte]
    registerPrimitive[Short, java.lang.Short]
    registerPrimitive[Int, java.lang.Integer]
    registerPrimitive[Long, java.lang.Long]
    registerPrimitive[Float, java.lang.Float]
    registerPrimitive[Double, java.lang.Double]
    registerPrimitive[UUID, java.util.UUID]


    registerInternal((Vector[Nothing]()).getClass, Pickler.VectorPickler, Unpickler.VectorUnpickler)
    register[Vector[Any]]

    registerInternal((Array[Nothing]()).getClass, Pickler.ArrayPickler, Unpickler.ArrayUnpickler)
    register[Array[Any]]
    register[Array[Double]]

//    register[Array[Double]]

    registerInternal((Map[Nothing, Nothing]()).getClass, Pickler.MapPickler, Unpickler.MapUnpickler)
    register[Map[Any, Any]]
    register[Map1[Any, Any]]
    register[Map2[Any, Any]]
    register[Map3[Any, Any]]
    register[Map4[Any, Any]]
    register[HashTrieMap[Any, Any]]
    registerInternal((Set[Nothing]()).getClass, Pickler.SetPickler, Unpickler.SetUnpickler)
    register[Set[Any]]
    register[Set1[Any]]
    register[Set2[Any]]
    register[Set3[Any]]
    register[Set4[Any]]
    register[HashTrieSet[Any]]

    register[String]
  }

  private def registerPrimitive[P : ClassTag, W : ClassTag](
      implicit pickler: Pickler[P], unpickler: Unpickler[P]): Unit = {
    register[P]
    registerInternal(implicitly[ClassTag[W]].runtimeClass, pickler, unpickler)
  }
}
