package org.scalawiki.wlx.dto

import org.scalawiki.dto.{Revision, Page, Template}
import org.specs2.mutable.Specification

class ImageSpec extends Specification {

  def makeTemplate(author: String, description: String = "") =
    new Template("Information",
      Map(
        "description" -> description,
        "date" -> "",
        "source" -> "{{own}}",
        "author" -> author,
        "permission" -> "",
        "other versions" -> ""
      )
    ).text

  "get Author" should {
    "get author from wiki link" in {
      val wiki = makeTemplate("[[User:Qammer Wazir|Qammer Wazir]]")
      Image.getAuthorFromPage(wiki) === "Qammer Wazir"
    }

    "get author from plain text" in {
      val wiki = makeTemplate("PhotoAmateur")
      Image.getAuthorFromPage(wiki) === "PhotoAmateur"
    }

  }

  "fromPageRevision" should {
    "parse" in {
      val wiki = makeTemplate("[[User:PhotoMaster|PhotoMaster]]", "{{Monument|nature-park-id}}")

      val page = Page("File:Image.jpg").copy(revisions = Seq(Revision.one(wiki)))
      val image = Image.fromPageRevision(page, "Monument", "2014-01-05").get

      image.author === Some("PhotoMaster")
      image.monumentId === Some("nature-park-id")
    }
  }

}