import akka.actor._
import akka.dispatch._
import akka.util.duration._


object ScalaFutureExample extends App {
  implicit val system = ActorSystem("FutureSystem")
  val bubbles = for (i <- 1 to 5) yield {
    Future {
      Thread.sleep(scala.util.Random.nextInt(1000))
      print(i)
      i
    }
  }
//  Thread.sleep(scala.util.Random.nextInt(1000))
  bubbles.map { Await.result(_, 1 second) }.foreach { print _ }
  system.shutdown()
}
