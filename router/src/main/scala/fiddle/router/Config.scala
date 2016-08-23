package fiddle.router

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

object Config {
  protected val config = ConfigFactory.load().getConfig("fiddle")

  val interface = config.getString("interface")
  val port = config.getInt("port")

  val corsOrigins = config.getStringList("corsOrigins").asScala

  object compiler {
    val c = config.getConfig("compiler")
    val host = c.getString("host")
    val port = c.getInt("port")
  }
}
