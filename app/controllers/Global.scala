import controllers.{routes, Application}
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import controllers.Utils._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(
      views.html.errorPage(ex)
    )
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(
      views.html.notFoundPage(request.path)
    )
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    if (request.headers.toMap.filter(x => x._1.toLowerCase.contains("propfind")).isEmpty) {
      lt(request.path)(super.onRouteRequest(request))(Play.isDev)
    } else {
      Some(Application.pi())
    }
  }
}