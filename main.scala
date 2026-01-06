//> using scala 3.7
//> using dep com.outr::scribe-cats::3.17.0
//> using dep org.http4s::http4s-ember-server::0.23.33
//> using dep org.http4s::http4s-dsl::0.23.33

import cats.effect.*
import cats.syntax.all.*
import concurrent.duration.*
import org.typelevel.ci.CIString
import com.comcast.ip4s.*
import scribecontext.Log as ScribeWithContext

class UserService(log: ScribeWithContext):
  def fetchUser(userId: String): IO[Unit] =
    log.annotate("user-id", userId) {
      for
        _ <- log.info(s"Fetching user")
        // Simulate fetching user
        _ <- IO.sleep(10.millis)
        _ <- log.info(s"Fetched user")
      yield ()
    }
end UserService

class ServiceRoutes(log: ScribeWithContext, userService: UserService):
  import org.http4s.dsl.io.*
  import org.http4s.HttpRoutes

  val routes = HttpRoutes
    .of[IO] { case req @ GET -> Root / "user" / userId =>
      log.annotate(
        req.headers
          .get(CIString("x-request-id"))
          .map(_.head.value)
          .map("request-id" -> _)
          .toMap
      ) {
        for
          _ <- log.info(s"Received request for user $userId")
          _ <- userService.fetchUser(userId)
          resp <- Ok(s"User $userId data")
        yield resp
      }
    }
    .orNotFound
end ServiceRoutes

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

object BasicExample extends IOApp.Simple:
  val randomDelay = cats.effect.std.Random
    .scalaUtilRandom[IO]
    .flatMap(_.nextIntBounded(1000))
    .flatMap(t => IO.sleep(t.millis))

  val run =
    ScribeWithContext.io.flatMap { log =>
      List("UserA", "UserB", "UserC").parTraverse { userId =>
        log.annotate(
          Map("session-id" -> java.util.UUID.randomUUID().toString)
        ) {
          log.annotate("user-id", userId) {
            log.annotate("request-id", java.util.UUID.randomUUID().toString) {
              for
                _ <- randomDelay
                _ <- log.info(s"fetching user $userId from database")
                _ <- randomDelay
                _ <- log.warn(s"downloading profile picture for user $userId")
              yield ()
            }
          }
        }
      }
    }.void
end BasicExample
