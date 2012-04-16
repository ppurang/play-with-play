import org.specs2.mutable._

import play.api.libs.concurrent.Promise
import play.api.libs.ws.{Response, WS}
import play.api.test._
import play.api.test.Helpers._

class ServerCachedPiSpec extends Specification {

 /* "run in a server" in {
    running(TestServer(3333)) {
      {
        val promise: Promise[Response] = WS.url("http://localhost:3333/cached/pi").get
        val r: Response = await(promise)
        r.status must equalTo(OK)
        r.body contains "3.141592653589793"
      }

      {
        val promise: Promise[Response] = WS.url("http://localhost:3333/cached/pi").get
        val r: Response = await(promise)
        r.status must equalTo(OK)
        r.body contains "Cached"
      }



    }
  }*/
}
