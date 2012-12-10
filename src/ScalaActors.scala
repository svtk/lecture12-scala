import actors._

object ScalaActors extends App {

  object SillyActor extends Actor {
    def act() {
      for (i <- 1 to 3) {
        println("I'm acting!")
        CommunicativeActor ! "Hi there"
        Thread.sleep(1000)
      }
    }
  }

  SillyActor.start()

  object SeriousActor extends Actor {
    def act() {
      for (i <- 1 to 3) {
        println("To be or not to be.")
        Thread.sleep(1000)
      }
    }
  }

  SeriousActor.start()





  object CommunicativeActor extends Actor {
    def act() {
      loop {
        react {
          case msg => println("received message: " + msg)
        }
        println("I'm here!!!")
      }
    }
  }
  CommunicativeActor.start()

}
