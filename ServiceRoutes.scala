import cats.effect.*
import cats.syntax.all.*
import concurrent.duration.*
import org.typelevel.ci.CIString
import com.comcast.ip4s.*
import scribecontext.Log as ScribeWithContext

class ServiceRoutes(log: ScribeWithContext, userService: UserService):
  import org.http4s.dsl.io.*
  import org.http4s.{HttpApp, HttpRoutes}

  lazy val routes = withRequestId(base)

  private val withRequestId: HttpApp[IO] => HttpApp[IO] =
    original =>
      HttpApp[IO]: req =>
        log.annotate(
          req.headers
            .get(CIString("x-request-id"))
            .map(_.head.value)
            .map("request-id" -> _)
            .toMap
        ):
          original(req)

  private val base =
    HttpRoutes
      .of[IO]:
        case req @ GET -> Root / "user" / userId =>
          for
            _ <- log.info(s"Received request for user $userId")
            _ <- userService.fetchUser(userId)
            resp <- Ok(s"User $userId data")
          yield resp
      .orNotFound
end ServiceRoutes