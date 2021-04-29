package com.leellc.faqconverter

import cats.effect.{Blocker, ContextShift, Sync, IO}
import cats.implicits._
import fs2.{Stream, io, text}
import java.nio.file.Path

trait FaqConverter[F[_]] {

  def convert(input: Path, output: Path): F[Unit]

}

object FaqConverter {

  case class FaqRecord(faqId: String, productId: String) {
    override def toString(): String = s"$faqId,$productId"
  }

  def apply[F[_]: Sync: ContextShift](blocker: Blocker): F[FaqConverter[F]] = {

    Sync[F].delay {

      new FaqConverter[F] {
        override def convert(input: Path, output: Path): F[Unit] = {

          def recordStringToFaqRecords(recordString: String): List[FaqRecord] = {
            val faqWithProducts: List[String] = recordString.split(",").map(_.trim).toList
            val faqId = faqWithProducts.head
            val productIds = faqWithProducts.tail
            // val fixError1 =
            //   productIds.flatMap(s => if (s.contains(".")) s.split(".").map(_.trim).toList else List(s))
            // val fixError2 = fixError1.flatMap(s =>
            //   if (s.startsWith("NP0") && s.size == 18) s.splitAt(9).productIterator.toList.map(_.toString)
            //   else List(s)
            // )
            // fixError2.map(pid => FaqRecord(faqId, pid))
            productIds.map(pid => FaqRecord(faqId, pid));
          }

          val result: Stream[F, Unit] = io.file
            .readAll[F](input, blocker, 4096)
            .through(text.utf8Decode)
            .through(text.lines)
            .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
            .map(line => recordStringToFaqRecords(line).mkString("\n"))
            .intersperse("\n")
            .through(text.utf8Encode)
            .through(io.file.writeAll(output, blocker))

          result.compile.drain
        }
      }
    }
  }
}
