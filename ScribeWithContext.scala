package scribecontext

import cats.effect.*
import scribe.{cats as _, *}

class Log private (
    orig: Scribe[IO],
    loc: IOLocal[Map[String, String]]
) extends Scribe[IO]:

  def annotate[A](key: String, value: String)(io: IO[A]) =
    loc.modify(m => (m.updated(key, value), m)).flatMap { prev =>
      io.guarantee(loc.set(prev))
    }

  def annotate[A](context: Map[String, String])(io: IO[A]) =
    loc.modify(m => (m ++ context, m)).flatMap { prev =>
      io.guarantee(loc.set(prev))
    }

  override def log(record: => LogRecord): IO[Unit] =
    loc.get.flatMap { context =>
      val newRecord = context.foldLeft(record) { case (r, (k, v)) =>
        r.update(k, () => v)
      }
      orig.log(newRecord)
    }
end Log

object Log:
  def create(orig: Scribe[IO]): IO[Log] =
    IOLocal(Map.empty).map(new Log(orig, _))

  def io: IO[Log] = create(scribe.cats.io)
end Log
