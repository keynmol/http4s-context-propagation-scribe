import cats.effect.*
import cats.syntax.all.*

import concurrent.duration.*

object BasicExample extends IOApp.Simple:
  val randomDelay = cats.effect.std.Random
    .scalaUtilRandom[IO]
    .flatMap(_.nextIntBounded(1000))
    .flatMap(t => IO.sleep(t.millis))

  val run =
    scribecontext.Log.io.flatMap { log =>
      List("UserA", "UserB", "UserC").parTraverse { userId =>
        log.annotate(
          Map("session-id" -> java.util.UUID.randomUUID().toString)
        ) {
          log.annotate("user-id", userId) {
            log.annotate("trace-id", java.util.UUID.randomUUID().toString) {
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
