package client.wlx

import client.wlx.dto.Contest
import client.wlx.query.ImageQuery

class Statistics {

  def init(): Unit = {
    val wlmContest = Contest.WLMUkraine(2014, "09-15", "10-15")
    val allContests = (2012 to 2014).map(year =>  Contest.WLMUkraine(year, "09-01", "09-30"))
//    val allContests = Seq(wlmContest) ++ previousContests

    val monumentDb = MonumentDB.create(wlmContest)

    monumentDb.fetchLists()

    monumentDb.monuments.size

    val imageQuery = ImageQuery.create()

    val imageDbs = allContests.map{
      contest =>
        val images = imageQuery.imagesFromCategory(contest.category, contest)
        new ImageDB(contest, images)
    }

    val totalImages = imageQuery.imagesWithTemplate(wlmContest.fileTemplate, wlmContest)
    val totalImageDb = new ImageDB(wlmContest, totalImages)

    val output = new Output()

    val stat = output.monumentsPictured(imageDbs, totalImageDb, monumentDb)

    println(stat)
  }

}

object Statistics {
  def main(args: Array[String]) {
    new Statistics().init()
  }
}
