package fiddle.router

import akka.actor.ActorSystem

object Server extends App {
  val system = ActorSystem()

  val router = system.actorOf(Router.props(Config.compiler.host, Config.compiler.port))
  val webService = new WebService(system, router)
}
