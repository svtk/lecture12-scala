import actors._
import actors.Actor._

object SearchWithGatherer extends App {

  sealed trait SearchNodeMessage
  case class SearchQuery(query: String, maxResults: Int, gatherer: OutputChannel[QueryResponse]) extends SearchNodeMessage
  case class QueryResponse(docs: Seq[ScoredDocument])

  trait SearchNode extends Actor {
    val index: Map[String, Seq[ScoredDocument]]

    override def act() {
      loop {
        react {
          case SearchQuery(query, maxResults, requester) =>
            val result = index.get(query).getOrElse(Seq())
            requester ! QueryResponse(result.take(maxResults))
        }
      }
    }
  }

  trait GathererNode extends Actor {
    val maxDocs: Int
    val maxResponses: Int
    val client: OutputChannel[QueryResponse]

    override def act() {
      def combineResults(current: Seq[ScoredDocument], next: Seq[ScoredDocument]) =
        (current ++ next).view.sortBy(-_.score).take(maxDocs).force

      def bundleResult(curCount: Int, current: Seq[ScoredDocument]) {
        if (curCount < maxResponses) {
          receiveWithin(1000L) {
            case QueryResponse(results) =>
              bundleResult(curCount + 1,
                combineResults(current, results))
            case TIMEOUT =>
              bundleResult(maxResponses, current)
          }
        }
        else {
          client ! QueryResponse(current)
        }
      }

      bundleResult(0, Seq())
    }
  }

  trait HeadNode extends Actor {
    val nodes: Seq[OutputChannel[SearchNodeMessage]]

    override def act() = loop {
      react {
        case searchQuery@ SearchQuery(q, max, responder) => {
          val gatherer = new GathererNode {
            val maxDocs: Int = max
            val maxResponses: Int = nodes.size
            val client: OutputChannel[QueryResponse] = responder
          }
          self link gatherer
          gatherer.start()
          for (node <- nodes) {
            node ! SearchQuery(q, max, gatherer)
          }
        }
      }
    }
  }

  val searchNode1 = new SearchNode {
    val index = IndexData.searchIndex1
  }

  val searchNode2 = new SearchNode {
    val index = IndexData.searchIndex2
  }

  val headNode = new HeadNode {
    val nodes: Seq[SearchNode] = List(searchNode1, searchNode2)
  }

  headNode.start()
  searchNode1.start()
  searchNode2.start()

  val resultActor : Actor = actor {
    loop {
      react {
        case QueryResponse(docs) =>
          println(docs.map(_.document).mkString("\n"))
      }
    }
  }

  headNode ! SearchQuery("cat", 2, resultActor)
}
