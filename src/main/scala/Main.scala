
package com.leellc.faqconverter

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import java.nio.file.Path
import com.leellc.faq.app.FaqConverter

object Main
    extends CommandIOApp(
      name = "faq converter",
      header = "FAQ converter"
    ) {

    //private val opts: Opts[Path] = Opts.argument[Path]("file")
    private val input: Opts[Path] = Opts.option[Path]("in", short = "i", metavar = "inputFile", help = "Input CSV file.")
    private val output: Opts[Path] = Opts.option[Path]("out", short = "o", metavar = "outFile", help = "Output CSV file.")

    override def main: Opts[IO[ExitCode]] = {
        input.map { i =>
            FaqConverter.converter.compile.drain.as(ExitCode.Success)
        }
    }
        // opts.map { path =>
        // Blocker[IO]
        //     .use { blocker =>
        //     Slf4jLogger.create[IO].flatMap { implicit logger =>
        //         Renderer[IO](blocker).flatMap { renderer =>
        //         Analyzer.analyze[IO](blocker, path).flatMap(renderer.renderAnalysis)
        //         }
        //     }
        // }
        // .as(ExitCode.Success)

}