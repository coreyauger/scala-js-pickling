package be.doeraene.spickling

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ literal => lit }

import utest._

case class Person(name: String, age: Int)

case object TrivialCaseObject

object CaseClassPicklersTest extends PicklersTest {

  PicklerRegistry.register[Person]
  PicklerRegistry.register(TrivialCaseObject)

  val tests = TestSuite {
    "pickle a Person" - {
      expectPickleEqual(
          Person("Jack", 24),
          lit(t = "be.doeraene.spickling.Person", v = lit(
              name = lit(t = "java.lang.String", v = "Jack"),
              age = lit(t = "java.lang.Integer", v = 24))))
    }

    "pickle Array[Double]" - {
      expectPickleEqual(
        Array[Double](1,2),
        lit(t = "[D", v =  js.Array(1,2)))
    }

    "unpickle Array[Double]" - {
      expectUnpickleEqual(
        lit(t = "[D", v =  js.Array(2.0,3.0)),
        Array[Double](2.0,3.0))
    }


    "pickle vector" - {
      expectPickleEqual(
        Vector[Int](1,2),
        lit(t = "scala.collection.immutable.Vector", v =  js.Array(1,2)))
    }

    "unpickle vector" - {
      expectUnpickleEqual(
        lit(t = "scala.collection.immutable.Vector", v =  js.Array(
          1,2
          )
        ),
        Vector[Int](1,2))
    }

   /* "pickle Array[Double]" - {
      expectPickleEqual(
        Array[Double](3,4),
        lit(t = "[D", v =  js.Array(3,4)))
    }

    "unpickle Array[Double]" - {
      expectUnpickleEqual(
        lit(t = "[D", v =  js.Array(3,4)),
        Array[Double](3,4))
    }*/

    "unpickle a Person" - {
      expectUnpickleEqual(
          lit(t = "be.doeraene.spickling.Person", v = lit(
              name = lit(t = "java.lang.String", v = "Jack"),
              age = lit(t = "java.lang.Integer", v = 24))),
          Person("Jack", 24))
    }

    "pickle TrivialCaseObject" - {
      expectPickleEqual(
          TrivialCaseObject,
          lit(s = "be.doeraene.spickling.TrivialCaseObject$"))
    }

    "unpickle TrivialCaseObject" - {
      expectUnpickleEqual(
          lit(s = "be.doeraene.spickling.TrivialCaseObject$"),
          TrivialCaseObject)
    }
  }
}
