import cats.effect.*
import cats.syntax.all.*
import concurrent.duration.*
import org.typelevel.ci.CIString
import com.comcast.ip4s.*
import scribecontext.Log as ScribeWithContext

object ServerExample extends IOApp.Simple:
  import org.http4s.ember.server.EmberServerBuilder

  val run =
    ScribeWithContext.io.toResource.flatMap { log =>
      val userService = new UserService(log)
      val routes = new ServiceRoutes(log, userService).routes
      EmberServerBuilder
        .default[IO]
        .withHost(host"localhost")
        .withPort(port"8080")
        .withHttpApp(routes)
        .build
        .evalTap(server => log.info(s"Server started at ${server.baseUri}"))
    }.useForever
end ServerExample
