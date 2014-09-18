package client.wlx

import client.wlx.dto.{Contest, Image}

class ImageDB(val contest: Contest, val images: Seq[Image]) {

  val _byId: Map[String, Seq[Image]] = images.groupBy(_.monumentId.getOrElse(""))
  val _byRegion: Map[String, Seq[Image]] = images.groupBy(_.monumentId.getOrElse("").split("\\-")(0))
  val _idsByRegion: Map[String, Set[String]] = ids.groupBy(_.split("\\-")(0))

//  var allImages: Seq[Image] = Seq.empty

  def ids: Set[String] = _byId.keySet

  def byId(id: String) = _byId.getOrElse(id, Seq.empty[Image])

  def byRegion(regId: String) = images.filter(_.monumentId.fold(false)(_.startsWith(regId)))

}


