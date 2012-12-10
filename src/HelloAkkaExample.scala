import akka.actor.{ ActorSystem, Actor, Props }
import akka.dispatch.{Future, Await}
import akka.pattern.ask
import akka.util.duration._

case object Start

class HelloActor extends Actor {
  val worldActor = context.actorOf(Props[WorldActor])
  def receive = {
    case Start =>
      worldActor ! "Hello"
      val future: Future[Any] = worldActor.ask("Hello")(1 second)
      println("Received message: %s".format(Await.result(future, 1 second)))
      context.system.shutdown()
  }
}

class WorldActor extends Actor {
  def receive = {
    case s: String ⇒ sender ! s.toUpperCase + " world!"
  }
}

object HelloAkkaExample extends App {
  val system = ActorSystem()
  system.actorOf(Props[HelloActor]) ! Start
}
