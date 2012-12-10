import actors._

object Search extends App {

  case class SearchQuery(query: String, maxResults: Int)

  trait SearchNode extends Actor {
    val index: Map[String, Seq[ScoredDocument]]

    override def act() {
      loop {
        react {
          case SearchQuery(query, maxResults) =>
            reply (index.get(query).getOrElse(Seq()).take(maxResults))
        }
      }
    }
  }

  trait HeadNode extends Actor {
    val nodes: Seq[SearchNode]

    override def act() = loop {
      react {
        case searchQuery@ SearchQuery(_, maxResults) => {
          val futureResults: Seq[Future[Any]] =
            nodes.map(searchNode => searchNode !! searchQuery)

          def combineResults(current: Seq[ScoredDocument],
                             next: Seq[ScoredDocument]) = {
            (current ++ next).view.sortBy(-_.score).
              take(maxResults).force
          }

          reply (futureResults.foldLeft(Seq[ScoredDocument]()) {
            case (current: Seq[ScoredDocument], next: Future[Any]) =>
              combineResults(current,
                next().asInstanceOf[Seq[ScoredDocument]])
          }.map(_.document))
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
    val nodes = List(searchNode1, searchNode2)
  }

  headNode.start()
  searchNode1.start()
  searchNode2.start()

  val future: Future[Any] = headNode !! SearchQuery("cat", 1)
  val result = future()
  println(result.asInstanceOf[Seq[String]].mkString("\n"))
}
