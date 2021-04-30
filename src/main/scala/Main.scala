
package com.leellc.faqconverter

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.data.Validated
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import java.nio.file.{Path, Paths, Files}

object Main
    extends CommandIOApp(
      name = "faqconverter",
      header = "FAQ converter"
    ) {

    private val input: Opts[Path] = Opts.option[Path]("in", short = "i", metavar = "inputFile", help = "Input CSV file.").mapValidated{ in =>
        if (Files.exists(in)) Validated.valid(in)
        else Validated.invalidNel(s"Error: file '$in' does not exist")
    }
    private val output: Opts[Path] = Opts.option[Path]("out", short = "o", metavar = "outFile", help = "Output CSV file. Default: output.csv")

    private val outputMaybe: Opts[Option[Path]] = output.orNone

    private val verboseOrNot = Opts.flag("verbose", help = "Print extra metadata to the console.").orFalse

    override def main: Opts[IO[ExitCode]] = {
        (input, outputMaybe, verboseOrNot).mapN { (i, o, v) =>
            Blocker[IO].use { blocker =>
                FaqConverter[IO](blocker).flatMap { converter =>
                    lazy val defaultPath = i.toAbsolutePath.toString().reverse.dropWhile(_ != '.').reverse + "out.csv"
                    converter.convert(i, o.getOrElse(Paths.get(defaultPath)))
                }.map { numRecords =>
                    if (v) println(s"Records processed: $numRecords") else ()
                }
            }.as(ExitCode.Success)
        }
    }
}