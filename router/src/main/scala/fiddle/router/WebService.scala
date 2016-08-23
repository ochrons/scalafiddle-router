package fiddle.router

import akka.actor._
import akka.pattern._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

class WebService(system: ActorSystem, router: ActorRef) {
  implicit val actorSystem = system
  implicit val timeout = Timeout(30.seconds)
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger(getClass)

  val route = {
    encodeResponse {
      get {
        path("embed") {
          extractRequest { request =>
            complete(ask(router, RouteRequest(request)).mapTo[HttpResponse])
          }
        } ~ path("codeframe") {
          extractRequest { request =>
            complete(ask(router, RouteRequest(request)).mapTo[HttpResponse])
          }
        } ~ path("compile") {
          extractRequest { request =>
            complete(ask(router, RouteRequest(request)).mapTo[HttpResponse])
          }
        } ~ path("complete") {
          extractRequest { request =>
            complete(ask(router, RouteRequest(request)).mapTo[HttpResponse])
          }
        } ~ path("cache") {
          extractRequest { request =>
            complete(ask(router, RouteRequest(request)).mapTo[HttpResponse])
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, Config.interface, Config.port)

  bindingFuture map { binding =>
    log.info(s"Web service started at ${Config.interface}:${Config.port}")
  } recover {
    case ex =>
      log.error(s"Failed to bind to ${Config.interface}:${Config.port}", ex)
  }
}
