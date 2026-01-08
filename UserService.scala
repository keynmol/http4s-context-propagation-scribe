import cats.effect.*
import concurrent.duration.*

class UserService(log: scribecontext.Log):
  def fetchUser(userId: String): IO[Unit] =
    log.annotate("user-id", userId):
      for
        _ <- log.info(s"Fetching user $userId")
        _ <- IO.sleep(10.millis)
        _ <- log.info(s"Fetched user $userId")
      yield ()
end UserService
