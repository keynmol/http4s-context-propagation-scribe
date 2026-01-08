import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.ember.client.EmberClientBuilder
import scribecontext.Log as ScribeWithContext

object ServerClientExample extends IOApp.Simple:
  import org.http4s.ember.server.EmberServerBuilder
  import org.http4s.dsl.io.*
  import org.http4s.implicits.*

  val run =
    (
      ScribeWithContext.io.toResource,
      EmberClientBuilder.default[IO].build
    ).flatMapN: (log, baseClient) =>
      val userService = new UserService(log)
      val middleware = RequestIdMiddleware(log)
      val client = middleware.client(baseClient)

      val baseRoutes =
        HttpRoutes
          .of[IO]:
            case GET -> Root / "user" / userId =>
              for
                _ <- log.info(s"Received request for user $userId")
                _ <- userService.fetchUser(userId)
                httpbin <- client.expect[String](
                  uri"http://httpbin.org/headers"
                )
                _ <- log.info(s"HTTPBin response: $httpbin")
                resp <- Ok(s"User $userId data")
              yield resp
          .orNotFound
      val routes = middleware.server(baseRoutes)

      EmberServerBuilder
        .default[IO]
        .withHost(host"localhost")
        .withPort(port"8080")
        .withHttpApp(routes)
        .build
        .evalTap(server => log.info(s"Server started at ${server.baseUri}"))
    .useForever
end ServerClientExample
