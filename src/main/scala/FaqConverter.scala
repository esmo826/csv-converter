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
            val faqId = recordString.takeWhile( _ != ',')
            val productIds = recordString.dropWhile( _ != '"').replaceAll("\"", "").split(",").map(_.trim).toList
            productIds.map(pid => FaqRecord(faqId, pid));
          }

          val countRefF = Ref.of[F, Int](0)

          countRefF.flatMap { ref =>
            val result: Stream[F, Unit] = io.file
            .readAll[F](input, blocker, 4096)
            .through(text.utf8Decode)
            .through(text.lines)
            .filter( s =>
              !s.trim.isEmpty && !s.startsWith("//") && !s.trim.contains("ANSWER ID,Product SKU")
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

            result.compile.drain.flatMap(_ => ref.get)
          }
        }
      }
    }
  }
}
