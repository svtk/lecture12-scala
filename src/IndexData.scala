case class ScoredDocument(score: Double, document: String)

//"cat", 2

object IndexData {

  val searchIndex1: Map[String, Seq[ScoredDocument]] = Map(
    "fox" ->
      List(
        ScoredDocument(0.4, "The fox condemns the trap, not himself."),
        ScoredDocument(0.3, "An old fox understands the trap.")),
    "cat" ->
      List(
        ScoredDocument(0.3, "Curiosity killed the cat, but for a while I was a suspect."),
        ScoredDocument(0.2, "When the cats away, the mice will play!")
      )
  )

  val searchIndex2: Map[String, Seq[ScoredDocument]] = Map(
    "fox" ->
      List(
        ScoredDocument(0.2, "The fox changes his fur but not his habits"),
        ScoredDocument(0.1, "The sleeping fox catches no poultry.")),
    "cat" ->
      List(
        ScoredDocument(0.4, "Time spent with cats is never wasted."),
        ScoredDocument(0.1, "A cat has absolute emotional honesty: human beings, for one reason or another, may hide their feelings, but a cat does not.")
      )

  )
}
