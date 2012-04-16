package controllers

import System.{nanoTime => tick}
import java.util.concurrent.TimeUnit.NANOSECONDS.{toMillis => ms}
import play.api.Logger


object Utils {

  trait Fold[A] {
    def fold[B](f: A => B,  g: => B): B
  }
  
  case class OptionFold[A](o: Option[A]) extends Fold[A] {
    def fold[B](f: A => B, g: => B) : B = o match {
      case Some(a) => f(a)
      case _ => g
    }
  }

  implicit def optionToOptionFold(o: Option[_]): Fold[_] = OptionFold(o)
  
  def time[A](f : => A)(enabled: Boolean = true): A = {
    if(enabled) {
      val start = tick
      val v = f
      val stop = tick
      Logger.info("It took %s ms".format(ms(start-stop)))
      v
    } else {
      f
    }
  }

}
