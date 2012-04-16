import org.specs2.mutable._

import play.api.libs.concurrent.Promise
import play.api.libs.ws.{Response, WS}
import play.api.mvc.AsyncResult
import play.api.test._
import play.api.test.Helpers._

class CachedPiSpec extends Specification {

  "cached pi CONTROLLER" in {
    running(FakeApplication()) {
      val r = await((controllers.Application.cachedPi()(FakeRequest())).asInstanceOf[AsyncResult].result)
      status(r) must equalTo(OK)
      contentType(r) must beSome("text/plain")
      charset(r) must beSome("utf-8")
      contentAsString(r) must contain("3.141592653589793")

      val result2 = controllers.Application.cachedPi()(FakeRequest())
      contentAsString(result2) must contain("Cached")
    }

  }

  "cached pi ROUTE" in {
    running(FakeApplication()) {
      val result = await(routeAndCall(FakeRequest(GET, "/cached/pi")).asInstanceOf[Some[AsyncResult]].get.result)

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/plain")
      charset(result) must beSome("utf-8")
      contentAsString(result) must contain("3.141592653589793")
    }
  }


  "run in a server" in {
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
  }


}
