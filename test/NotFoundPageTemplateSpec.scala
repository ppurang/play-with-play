import org.specs2.mutable._

import play.api.test.Helpers._

class NotFoundPageTemplateSpec extends Specification {

  "render not found page template" in {
    val html = views.html.notFoundPage("/path/to/heaven")

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("/path/to/heaven")
  }

}
