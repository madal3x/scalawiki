package org.scalawiki.wlx

import org.scalawiki.cache.CachedBot
import org.scalawiki.dto.Site
import org.scalawiki.wlx.dto.{Contest, Country}
import org.scalawiki.wlx.query.MonumentQuery
import org.specs2.mutable.Specification

class WlmUaListsSpec extends Specification {

  val campaign = "wlm-ua"
  val contest = Contest.byCampaign(campaign).get.copy(year = 2019)
  val country = Country.Ukraine

  "lists" should {
    "be ok" in {
      contest.name === "Wiki Loves Monuments 2019 in Ukraine"
      val cacheName = s"$campaign-2019"
      val bot = new CachedBot(Site.ukWiki, cacheName + "-wiki", true, entries = 100)
      val monumentQuery = MonumentQuery.create(contest)(bot)
      val monumentDb = MonumentDB.getMonumentDb(contest, monumentQuery)

      val all = monumentDb.allMonuments
      all must not(beEmpty)
      println(s"all size: ${all.size}")
      val notFound = all.map { m =>
        s"${m.page}/${m.city.getOrElse("")}" -> country.byIdAndName(m.id, m.city.getOrElse(""))
      }.filter { case (m, cities) =>
        cities.isEmpty || cities.tail.nonEmpty
      }

      println(s"notFound size: ${notFound.size}")
      notFound.groupBy(_._1).mapValues { seq =>
        (seq.flatMap(_._2), seq.size)
      }.toSeq.sortBy { case (k, (v, s)) => -s}
        .map { case (k, (v, s)) =>
          val parents = v.map(_.parent().map(_.name).getOrElse("")).toSet.mkString(", ")
          s"$k - $s ($parents)"
        }
        .foreach(println)

      val percentage = notFound.size * 100 / all.size
      println(s"percentage: $percentage%")
      percentage should be < 10 // less than 10%
    }
  }
}
