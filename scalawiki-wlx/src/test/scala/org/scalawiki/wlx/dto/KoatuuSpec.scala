package org.scalawiki.wlx.dto

import org.specs2.mutable.Specification

class KoatuuSpec extends Specification {

  "Koatuu" should {

    val Ukraine = Country.Ukraine
    val regions = Ukraine.regions

    val topRegions = Map(
      "01" -> "Автономна Республіка Крим",
      "05" -> "Вінницька область",
      "07" -> "Волинська область",
      "12" -> "Дніпропетровська область",
      "14" -> "Донецька область",
      "18" -> "Житомирська область",
      "21" -> "Закарпатська область",
      "23" -> "Запорізька область",
      "26" -> "Івано-Франківська область",
      "32" -> "Київська область",
      "35" -> "Кіровоградська область",
      "44" -> "Луганська область",
      "46" -> "Львівська область",
      "48" -> "Миколаївська область",
      "51" -> "Одеська область",
      "53" -> "Полтавська область",
      "56" -> "Рівненська область",
      "59" -> "Сумська область",
      "61" -> "Тернопільська область",
      "65" -> "Херсонська область",
      "63" -> "Харківська область",
      "68" -> "Хмельницька область",
      "71" -> "Черкаська область",
      "73" -> "Чернівецька область",
      "74" -> "Чернігівська область",
      "80" -> "Київ",
      "85" -> "Севастополь"
    )

    "contain country parent" in {
      regions.flatMap(_.parent()) === List.fill(topRegions.size)(Country.Ukraine)
    }

      "contain level1 names" in {
      regions.map(_.name).toSet === topRegions.toSeq.map(_._2).toSet
    }

    "lookup level1 by code" in {
      Ukraine.byRegion(topRegions.keySet)
        .map { case (adm, ids) => ids.head -> adm.name } === topRegions
    }

    "contain Kyiv raions" in {
      val kyiv = regions.find(_.name == "Київ").get
      val regionNames = Seq("Райони м. Київ", "Голосіївський", "Дарницький", "Деснянський", "Дніпровський",
        "Оболонський", "Печерський", "Подільський", "Святошинський", "Солом'янський", "Шевченківський")
      kyiv.regions.map(_.name) === regionNames
      kyiv.regions.flatMap(_.parent().map(_.name)) === List.fill(regionNames.size)("Київ")
    }

    "find Kyiv raions by code" in {
      val idToName = Map(
        "80-300" -> "Райони м. Київ",
        "80-361" -> "Голосіївський",
        "80-363" -> "Дарницький",
        "80-364" -> "Деснянський",
        "80-366" -> "Дніпровський",
        "80-380" -> "Оболонський",
        "80-382" -> "Печерський",
        "80-385" -> "Подільський",
        "80-386" -> "Святошинський",
        "80-389" -> "Солом'янський",
        "80-391" -> "Шевченківський",
      )

      val regionToIds = Ukraine.byRegion(idToName.keySet)
      regionToIds.keySet.flatMap(_.parent().map(_.name)) === Set("Київ")

      regionToIds
        .map { case (adm, ids) => ids.head -> adm.name } === idToName
    }

    "contain Crimea regions" in {
      val crimea = regions.find(_.name == "Автономна Республіка Крим").get
      val regionNames = Seq(
        "Міста Автономної Республіки Крим",
        "Сімферополь", "Алушта", "Джанкой", "Євпаторія", "Керч",
        "Красноперекопськ", "Саки", "Армянськ", "Феодосія", "Судак", "Ялта",
        "Райони Автономної Республіки Крим",
        "Бахчисарайський район", "Білогірський район", "Джанкойський район", "Кіровський район", "Красногвардійський район",
        "Красноперекопський район", "Ленінський район", "Нижньогірський район", "Первомайський район", "Роздольненський район",
        "Сакський район", "Сімферопольський район", "Совєтський район", "Чорноморський район")
      crimea.regions.map(_.name) === regionNames

      crimea.regions.flatMap(_.parent().map(_.name)) === List.fill(regionNames.size)("Автономна Республіка Крим")

    }

    "contain Vinnytsya oblast regions" in {
      val crimea = regions.find(_.name == "Вінницька область").get
      val regionNames = Seq(
        "Міста обласного підпорядкування Вінницької області",
        "Вінниця", "Жмеринка", "Могилів-Подільський", "Козятин", "Ладижин", "Хмільник",
        "Райони Вінницької області",
        "Барський район", "Бершадський район", "Вінницький район", "Гайсинський район", "Жмеринський район",
        "Іллінецький район", "Козятинський район", "Калинівський район", "Крижопільський район", "Липовецький район",
        "Літинський район", "Могилів-Подільський район", "Мурованокуриловецький район", "Немирівський район",
        "Оратівський район", "Піщанський район", "Погребищенський район", "Теплицький район", "Томашпільський район",
        "Тростянецький район", "Тульчинський район", "Тиврівський район", "Хмільницький район", "Чернівецький район",
        "Чечельницький район", "Шаргородський район", "Ямпільський район")
      crimea.regions.map(_.name) === regionNames

      crimea.regions.flatMap(_.parent().map(_.name)) === List.fill(regionNames.size)("Вінницька область")
    }

    "lookup regions by monumentId" in {
      val r1 = Ukraine.byId("14-215-0078").get
      r1.name === "Волноваський район"
      r1.parent().get.name === "Донецька область"

      val r2 = Ukraine.byId("26-252-0002").get
      r2.name === "Снятинський район"
      r2.parent().get.name === "Івано-Франківська область"
    }

  }
}
