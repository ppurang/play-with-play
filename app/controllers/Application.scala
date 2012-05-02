package controllers

import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import anorm.NotAssigned
import models.{UUIDKeyGen, RegisteredUriPlayer, Bar}
import org.h2.jdbc.JdbcSQLException
import play.api.libs.concurrent.{Promise, Akka}
import play.api.Play.current
import java.util.concurrent.TimeUnit
import Utils._
import play.api.cache.{Cache, Cached}
import play.api.templates.Html
import play.api.libs.Comet
import collection.immutable.Stream
import play.api.mvc.{WebSocket, Result, Action, Controller}
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import collection.mutable.ArrayBuffer

object Application extends Controller {

  val barForm = Form(
    single("name" -> nonEmptyText)
  )

  val urlForm = Form(
    single("location" -> nonEmptyText)
  )

  def index = Action {
    Ok(views.html.index(barForm))
  }


  def addBar() = Action {
    implicit request =>
      barForm.bindFromRequest.fold(
      errors => BadRequest, {
        case (name) =>
          Bar.create(Bar(NotAssigned, name))
          Redirect(routes.Application.index())
      }
      )
  }

  @volatile private var timeout = false

  def pi() = Action {
    Async {
      lt("-- pi async") {
        Akka.future {
          lt("---- pi future") {
            if (timeout) {
              TimeUnit.MILLISECONDS.sleep(5000);
              timeout = false
            } else {
              TimeUnit.MILLISECONDS.sleep(500);
              timeout = true
            }
            UUIDKeyGen.gen + " " + java.lang.Math.PI
          } {
            play.api.Play.isDev
          }
        } orTimeout("oops.. (like in Independece Day)", 1000) map {
          piOrTimeOut => piOrTimeOut fold(
            pi => Ok("PI value computed: " + pi),
            timeout => InternalServerError(timeout)
            )
        }
      } {
        play.api.Play.isDev
      }
    }
  }


  def cachedPi() = Cache.get("pi").fold(
  x => Action {
    Ok("Cached: " + x)
  }, {
    Action {
      Async {
        Akka.future {
          TimeUnit.MILLISECONDS.sleep(500);
          UUIDKeyGen.gen + " " + java.lang.Math.PI
        } orTimeout("oops.. (like in Independece Day)", 1000) map {
          piOrTimeOut => piOrTimeOut fold(
            pi => {
              Cache.set("pi", pi);
              Ok("PI value computed: " + pi)
            },
            timeout => InternalServerError(timeout)
            )
        }
      }
    }
  }
  )


  import com.codahale.jerkson.Json

  def listBars() = Action {
    val bars = Bar.findAll()

    val json = Json.generate(bars)

    Ok(json).as("application/json")
  }


  def listPlayers() = Action {
    Ok(Json.generate(RegisteredUriPlayer.findAll())).as("application/json")
  }

  def addUrl() = Action {
    implicit request =>
      urlForm.bindFromRequest.fold(
      errors => BadRequest, {
        case (location) =>
          val player: RegisteredUriPlayer = RegisteredUriPlayer(location, UUIDKeyGen.gen)
          val create: Either[Throwable, Int] = RegisteredUriPlayer.create(player)
          create.fold(
            t => BadRequest(t.asInstanceOf[JdbcSQLException].getErrorCode + " " + t.getMessage.split(":")(0)),
            i => Ok(views.html.showRegisteredPlayer(player))
          )
      }
      )
  }

  def addUrlShowForm = Action {
    Ok(views.html.addPlayer(urlForm))
  }


  def stream = Action {
    Ok.stream(
      Enumerator("kiki", "foo", "bar").andThen(Enumerator.eof)
    )
  }


  def oldComet = Action {
    val events = Enumerator(
      """<script>console.log('kiki')</script>""",
      """<script>console.log('foo')</script>""",
      """<script>console.log('bar')</script>"""
    )
    Ok.stream(events >>> Enumerator.eof).as(HTML)
  }

  // Transform a String message into an Html script tag
  val toCometMessage = Enumeratee.map[String] {
    data =>
      Html("""<script>console.log('""" + data + """')</script>""")
  }

  def comet = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.stream(events >>> Enumerator.eof &> toCometMessage)
  }

  def cometComet = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.stream(events &> Comet(callback = "console.log"))
  }


  def cometBrowser = Action {
    val continually: Stream[String] = scala.Stream.continually({
      "kiki"
    })
    import play.api.libs.Comet.CometMessage.stringMessages
    //val events = Enumerator(continually.take(1000))
    val events = Enumerator("tom", "dick", "harry")

    Ok.stream(events &> Comet(callback = "parent.cometMessage"))

  }

  def cometIFrame = Action {
    Ok(views.html.comet())
  }


  def webSocket = WebSocket.using[String] {
    request =>

    // Log events to the console
      val in = Iteratee.foreach[String](x => x).mapDone {
        _ =>
          println("Disconnected")
      }

      println("IN -> " + in)

      val hello = Enumeratee.map[String](x => "Hello " + x + "!")


      val out = Enumerator("abcd", "efgh", "ijkl")

      (in, out)
  }

  def webSocketTest = Action {
    Ok(views.html.ws())
  }


}