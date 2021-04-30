package com.leellc.faqconverter

import cats.effect.{Blocker, ContextShift, Sync, IO}
import cats.implicits._
import cats.effect.concurrent._
import fs2.{Stream, io, text}
import java.nio.file.Path
import cats.instances.boolean

trait FaqConverter[F[_]] {
  def convert(input: Path, output: Path): F[Int]
}

object FaqConverter {

  case class FaqRecord(faqId: String, productId: String) {
    override def toString(): String = s"$faqId,$productId"
  }

  def apply[F[_]: Sync: ContextShift](blocker: Blocker): F[FaqConverter[F]] = {

    Sync[F].delay {

      new FaqConverter[F] {
        override def convert(input: Path, output: Path): F[Int] = {

          def recordStringToFaqRecords(recordString: String): List[FaqRecord] = {

            // Does the line contain quotes or just commas?
            val (faqId, productIds) = if (recordString.contains("\"")) {
              val fId = recordString.takeWhile( _ != ',')
              val pIds = recordString.dropWhile( _ != '"').replaceAll("\"", "").split(",").map(_.trim).toList
              (fId, pIds)
            } else {
              val faqWithProducts: List[String] = recordString.split(",").map(_.trim).toList
              faqWithProducts match {
                case h :: tl => (h, tl)
                case Nil => (recordString, List.empty)
              }
            }

            productIds.map(pid => FaqRecord(faqId, pid));
          }

          // Create a mutable state to be updated in the stream
          val countRefF = Ref.of[F, Int](0)

          // Lift the countrRefF into a single element Stream
          Stream.eval(countRefF).flatMap { ref =>
            val result: Stream[F, Unit] = io.file
            .readAll[F](input, blocker, 4096)
            .through(text.utf8Decode)
            .through(text.lines)
            .filter( s =>
              !s.trim.isEmpty && !s.startsWith("//") && !s.trim.toLowerCase.contains("sku")
            )
            .evalTap { _ =>
              ref.update(_ + 1)
            }
            .map { line =>
              recordStringToFaqRecords(line).mkString("\n")
            }
            .intersperse("\n")
            .through(text.utf8Encode)
            .through(io.file.writeAll(output, blocker))

            // Drain will throw away the units
            val stream1 = result.drain // Stream[F, INothing]

            // Emit a single value (the record count) to the stream
            val stream2: Stream[F, Int] = Stream.eval(ref.get)

            // This is a neat trick. Since we only care about the last value of the stream (our count) we can
            // just append the streams together.
            stream1 ++ stream2
          // Since we are returning an F[A], you have to compile (Takes us out of Stream world, and into F)
          // `lastOrError` just returns the last element of the stream, or errors out if there is not one.
          }.compile.lastOrError

          //result.compile.drain.flatMap(_ => ref.get)

        }
      }
    }
  }
}
