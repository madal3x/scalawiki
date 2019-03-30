package org.scalawiki.wlx.stat

trait Rater {
  def rate(monumentId: String, author: String): Int
}

object NumberOfMonuments extends Rater {
  override def rate(monumentId: String, author: String) = 1
}

class NewlyPicturedBonus(oldMonumentIds: Set[String], newlyPicturedRate: Int) extends Rater {
  override def rate(monumentId: String, author: String): Int = {
    monumentId match {
      case id if !oldMonumentIds.contains(id) =>
        newlyPicturedRate
      case _ =>
        1
    }
  }
}

class NewlyPicturedPerAuthorBonus(oldMonumentIds: Set[String],
                                  oldMonumentIdsByAuthor: Map[String, Set[String]],
                                  newlyPicturedRate: Int,
                                  newlyPicturedPerAuthorRate: Int) extends Rater {
  override def rate(monumentId: String, author: String): Int = {
    monumentId match {
      case id if !oldMonumentIds.contains(id) =>
        newlyPicturedRate
      case id if !oldMonumentIdsByAuthor.getOrElse(author, Set.empty).contains(id) =>
        newlyPicturedPerAuthorRate
      case _ =>
        1
    }
  }
}

class NumberOfAuthorsBonus(authorsByMonument: Map[String, Int]) extends Rater {
  override def rate(monumentId: String, author: String): Int = {
    authorsByMonument.getOrElse(monumentId, 0) match {
      case 0 =>
        5
      case x if (1 to 3) contains x =>
        2
      case x if (4 to 9) contains x =>
        1
      case _ =>
        0
    }
  }
}

class NumberOfImagesInPlaceBonus(imagesPerPlace: Map[String, Int],
                                 placePerMonument: Map[String, String]) extends Rater {
  override def rate(monumentId: String, author: String): Int = {
    placePerMonument.get(monumentId).map { place =>
      imagesPerPlace.getOrElse(place, 0) match {
        case 0 =>
          4
        case x if (1 to 9) contains x =>
          2
        case x if (10 to 49) contains x =>
          1
        case _ =>
          0
      }
    }.getOrElse(0)
  }
}

class RateSum(raters: Seq[Rater]) extends Rater {
  override def rate(monumentId: String, author: String): Int = {
    raters.map(_.rate(monumentId, author)).sum
  }
}