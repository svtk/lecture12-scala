import scala.actors._
import scala.actors.Actor._
import collection.mutable.ArrayBuffer

class ReactActor extends Actor {
  def act() {
    loop {
      react {
        case 'Hello => println("React: " + Thread.currentThread.getId)
//        act()
      }
    }
  }
}

class ReceiveActor extends Actor {
  def act() {
    while (true) {
      receive {
        case 'Hello => {
          println("Receive: " + Thread.currentThread.getId)
          1
        }
      }
    }
  }
}

object ReceiveVsReact extends App {
  val actors = new ArrayBuffer[Actor]
  for (i <- 0 to 20) {
    actors += new ReactActor
    actors += new ReceiveActor
  }
  for (a <- actors) a.start()
  actor {
    println(Thread.currentThread.getId)
    for (a <- actors) a ! 'Hello
  }
}

