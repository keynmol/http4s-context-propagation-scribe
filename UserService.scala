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