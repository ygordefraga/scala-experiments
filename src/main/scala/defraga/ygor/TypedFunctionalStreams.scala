package defraga.ygor

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import fs2.{Chunk, INothing, Pipe, Pull, Pure, Stream}

import cats.effect.unsafe.implicits.global

object TypedFunctionalStreams extends IOApp {
  case class Actor(id: Int, firstName: String, lastName: String)

  object Data {
    // Justice League
    val galGodot: Actor = Actor(1, "Gal", "Godot")
    val henryCavil: Actor = Actor(0, "Henry", "Cavill")
    val ezraMiller: Actor = Actor(2, "Ezra", "Miller")
    val benFisher: Actor = Actor(3, "Ben", "Fisher")
    val rayHardy: Actor = Actor(4, "Ray", "Hardy")
    val jasonMomoa: Actor = Actor(5, "Jason", "Momoa")

    // Avengers
    val scarlettJohansson: Actor = Actor(6, "Scarlett", "Johansson")
    val robertDowneyJr: Actor = Actor(7, "Robert", "Downey Jr.")
    val chrisEvans: Actor = Actor(8, "Chris", "Evans")
    val markRuffalo: Actor = Actor(9, "Mark", "Ruffalo")
    val chrisHemsworth: Actor = Actor(10, "Chris", "Hemsworth")
    val jeremyRenner: Actor = Actor(11, "Jeremy", "Renner")
    val tomHolland: Actor = Actor(13, "Tom", "Holland")
    val tobeyMaguire: Actor = Actor(14, "Tobey", "Maguire")
    val andrewGarfield: Actor = Actor(15, "Andrew", "Garfield")
  }

  import Data._

  // streams
  val justiceLeagueActors: Stream[Pure, Actor] = Stream(
    galGodot,
    henryCavil,
    ezraMiller,
    benFisher,
    rayHardy,
    jasonMomoa
  )
  val tomHollandStream: Stream[Pure, Actor] = Stream.emit(tomHolland)

  implicit class IODebugOps[A](io: IO[A]) {
    def debug: IO[A] = io.map { a =>
      println(s"[${Thread.currentThread().getName}] $a")
      a
    }
  }

  val spiderMen: Stream[Pure, Actor] = Stream.emits {
    List(tomHolland, andrewGarfield, tobeyMaguire)
  }

  // convert a stream to a std data structure
  val justiceLeagueActorsList: Seq[Actor] = justiceLeagueActors.toList

  // infinite streams
  val infiniteJusticeLeagueActors: Stream[Pure, Actor] = justiceLeagueActors.repeat
  val repeatedJusticeLeagueActorsList: List[Actor] = infiniteJusticeLeagueActors.take(10).toList

  // effectful streams
  val savingTomHolland: Stream[IO, Actor] = Stream.eval {
    IO {
      println("Saving actor Tom Holland into the DB")
      Thread.sleep(1000)
      tomHolland
    }
  }

  // chunks
  val avengersActors: Stream[Pure, Actor] = Stream.chunk(Chunk.array(Array(
    scarlettJohansson,
    robertDowneyJr,
    chrisEvans,
    markRuffalo,
    chrisHemsworth,
    jeremyRenner,
    tomHolland,
    tobeyMaguire,
    andrewGarfield
  )))

  // transformations
  val all: Stream[Pure, Actor] = justiceLeagueActors ++ avengersActors

  // flat map
  val printedJlActors: Stream[IO, Unit] = justiceLeagueActors.flatMap { actor =>
    Stream.eval(IO.println(actor))
  }

  // flat map + eval = evalMap
  val printedJlActorsV2: Stream[IO, Unit] = justiceLeagueActors.evalMap(IO.println)

  // flat map + eval and keeping original type = evalTap
  val printedJlActorsV3: Stream[IO, Actor] = all.evalTap(IO.println)

  // compile
  val compiledStream: IO[Unit] = printedJlActorsV3.compile.drain

  override def run(args: List[String]): IO[ExitCode] =
    compiledStream
      .as(ExitCode.Success)
}
