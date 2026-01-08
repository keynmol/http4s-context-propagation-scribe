import cats.effect.*
import org.http4s.Header
import org.http4s.HttpApp
import org.http4s.client.Client
import org.typelevel.ci.CIString
import scribecontext.Log as ScribeWithContext

class RequestIdMiddleware(log: ScribeWithContext):

  private val ContextKey = "trace-id"
  private val HeaderKey = CIString("x-trace-id")

  def server(app: HttpApp[IO]): HttpApp[IO] =
    HttpApp: req =>
      log.annotate(
        req.headers
          .get(HeaderKey)
          .map(_.head.value)
          .map(ContextKey -> _)
          .toMap
      )(app(req))

  def client(base: Client[IO]): Client[IO] =
    Client[IO]: req =>
      val modifiedRequest =
        log.getContext
          .map(_.get(ContextKey))
          .map:
            case None        => req
            case Some(reqId) => req.putHeaders(Header.Raw(HeaderKey, reqId))

      modifiedRequest.toResource.flatMap: req =>
        base.run(req)
end RequestIdMiddleware
