
package com.leellc.faqconverter

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import java.nio.file.Path

object Main
    extends CommandIOApp(
      name = "faq converter",
      header = "FAQ converter"
    ) {

    private val input: Opts[Path] = Opts.option[Path]("in", short = "i", metavar = "inputFile", help = "Input CSV file.")
    private val output: Opts[Path] = Opts.option[Path]("out", short = "o", metavar = "outFile", help = "Output CSV file.")

    override def main: Opts[IO[ExitCode]] = {
        (input, output).mapN { (i, o) =>
            Blocker[IO].use { blocker =>
                FaqConverter[IO](blocker).flatMap { converter =>
                    converter.convert(i, o)
                }
            }.as(ExitCode.Success)
        }
    }
}