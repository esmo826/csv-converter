package com.leellc.faq.app

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import fs2.{Stream, io, text}
import java.nio.file.Paths

object FaqConverter extends IOApp {

  case class FaqRecord(faqId: String, productId: String) {
    override def toString(): String = s"$faqId,$productId"
  }

  val converter: Stream[IO, Unit] = Stream.resource(Blocker[IO]).flatMap { blocker =>
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

    io.file
      .readAll[IO](Paths.get("/Users/elee3389k/Downloads/test.csv"), blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => recordStringToFaqRecords(line).mkString("\n"))
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get("/Users/elee3389k/Downloads/out.csv"), blocker))
  }

  def run(args: List[String]): IO[ExitCode] = {
    converter.compile.drain.as(ExitCode.Success)
  }
}

