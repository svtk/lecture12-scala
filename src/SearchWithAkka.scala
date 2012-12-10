import akka.actor._
import akka.util.duration._

object SearchWithAkka extends App {

  sealed trait SearchNodeMessage
  case class SearchQuery(query: String, maxResults: Int, gatherer: ActorRef) extends SearchNodeMessage
  case class QueryResponse(docs: Seq[ScoredDocument])

  class SearchNode(val index: Map[String, Seq[ScoredDocument]]) extends Actor {

    override def receive = {
      case SearchQuery(query, maxResults, requester) =>
        val result = index.get(query).getOrElse(Seq())
        requester ! QueryResponse(result.take(maxResults))
    }
  }

  class GathererNode(
      val maxDocs: Int,
      val query: String,
      val maxResponses: Int,
      val client: ActorRef
  ) extends Actor {

    context.setReceiveTimeout(1 second)
    var results = Seq[ScoredDocument]()
    var responseCount = 0
    private def combineResults(current : Seq[ScoredDocument], next : Seq[ScoredDocument]) =
      (current ++ next).view.sortBy(- _.score).take(maxDocs).force

    def receive = {
      case QueryResponse(next) =>
        results = combineResults(results, next)
        responseCount += 1
        if(responseCount == maxResponses) {
          client ! QueryResponse(results)
          context.stop(self)
        }
        ()
      case ReceiveTimeout =>
        client ! QueryResponse(Seq())
        context.stop(self)
    }
  }

  class HeadNode(val nodes: Seq[ActorRef]) extends Actor {

    override def receive = {
      case searchQuery@SearchQuery(q, max, responder) =>
      {
        val gatherer = system.actorOf(Props(new GathererNode(max, q, nodes.size, responder)))
        for (node <- nodes) {
          node ! SearchQuery(q, max, gatherer)
        }
      }
    }
  }

  val system = ActorSystem()
  val searchNode1 = system.actorOf(Props(new SearchNode(IndexData.searchIndex1)), name = "SearchNode1")
  val searchNode2 = system.actorOf(Props(new SearchNode(IndexData.searchIndex2)), name = "SearchNode2")
  val searchNodes: List[ActorRef] = List(searchNode1, searchNode2)
  val headNode = system.actorOf(Props(new HeadNode(searchNodes)), name = "HeadNode")

  val resultActor = system.actorOf(Props(new Actor {
    def receive = {
      case QueryResponse(docs) => println(docs.map(_.document).mkString("\n"))
      system.shutdown()
    }
  }))

  headNode ! SearchQuery("cat", 2, resultActor)
}
