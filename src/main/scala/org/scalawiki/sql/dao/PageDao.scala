package org.scalawiki.sql.dao

import org.scalawiki.dto.{Page, Revision}
import org.scalawiki.sql.MwDatabase
import org.scalawiki.wlx.dto.Image
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds
import spray.util.pimpFuture

class PageDao(val mwDb: MwDatabase, val driver: JdbcProfile) {

  import driver.api._

  val pages = mwDb.pages
  val revisions = mwDb.revisions
  val texts = mwDb.texts
  val images = mwDb.images

  val revisionDao = mwDb.revisionDao
  val imageDao = mwDb.imageDao

  private val autoInc = pages returning pages.map(_.id)

  val db = mwDb.db

  def insertAll(pageSeq: Seq[Page]): Unit = {
    pages.forceInsertAll(pageSeq)

    revisionDao.insertAll(pageSeq.flatMap(_.revisions.headOption))
  }

  def insert(page: Page): Long = {
    require(page.revisions.nonEmpty, "page has no revisions")
    val newRevs = page.revisions //.filter(_.revId.isEmpty)

    val pageIdF = (
      if (page.id.isDefined) {
          if (exists(page.id.get))
            mwDb.db.run(pages.forceInsert(page)).map(_ => page.id)
          else
            Future.successful(page.id)
      }
      else {
        mwDb.db.run(autoInc += page)
      }
      ).map(_.get)

    pageIdF.map { pageId =>

      addRevisions(pageId, newRevs)
      addImages(pageId, page.images)

      pageId
    }.await
  }

  def addRevisions(pageId: Long, newRevs: Seq[Revision]) = {
    val revIds = newRevs.reverse.map { rev =>
      val withPage = rev.copy(pageId = Some(pageId))
      revisionDao.insert(withPage)
    }

    pages.filter(_.id === pageId)
      .map(p => p.pageLatest)
      .update(revIds.last)
  }

  def addImages(pageId: Long, images: Seq[Image]) = {
    images.reverse.foreach { image =>
      val withPage = image.copy(pageId = Some(pageId))
      imageDao.insert(withPage)
    }
  }

  def list = pages.sortBy(_.id)

  def get(id: Long): Option[Page] =
    db.run(pages.filter(_.id === id).result.headOption).await

  def exists(id: Long): Boolean =
    get(id).fold (false)(_ => true)


  def find(ids: Iterable[Long]): Seq[Page] =
    db.run(
      pages.filter(_.id inSet ids).sortBy(_.id).result
    ).await

  def findWithText(ids: Iterable[Long]): Seq[Page] =
    db.run(
      (pages.filter(_.id inSet ids)
        join revisions on (_.pageLatest === _.id)
        join texts on (_._2.textId === _.id)
        sortBy { case ((p, r), t) => p.id }
        ).result
    ).map { pages =>
      pages.map { case ((p, r), t) => p.copy(revisions = Seq(r.copy(content = Some(t.text)))) }
    }.await

  def findByRevIds(ids: Iterable[Long], revIds: Iterable[Long]): Seq[Page] = {
    db.run(
      (pages.filter(_.id inSet ids)
        join revisions.filter(_.id inSet revIds) on (_.id === _.pageId)
        join texts on (_._2.textId === _.id)
        joinLeft images on (_._1._1.id === _.pageId)
        ).sortBy { case (((p, r), t), i) => p.id }.result).map { pages =>
      pages.map {
        case (((p, r), t), i) => p.copy(revisions = Seq(r.copy(content = Some(t.text))), images = i.toSeq)
      }
    }.await
  }

  def withText(id: Long): Page =
    db.run(
      (pages.filter(_.id === id)
        join revisions on (_.pageLatest === _.id)
        join texts on (_._2.textId === _.id)
        ).result).map { pages =>
      pages.map {
        case ((p, r), t) => p.copy(revisions = Seq(r.copy(content = Some(t.text))))
      }.head
    }.await

  def withRevisions(id: Long): Page = {
    db.run(((
      for {
        p <- pages if p.id === id
        r <- revisions if r.pageId === p.id
        t <- texts if r.textId === t.id
      } yield (p, r, t)
      ) sortBy { case (p, r, t) => r.id.desc }
      ).result).map { pages =>
      val rows = pages.map {
        case (p, r, t) => (p, r.copy(content = Some(t.text)))
      }
      val revs = rows.map { case (p, r) => r }
      rows.headOption.map { case (p, r) => p.copy(revisions = revs) }.get
    }
  }.await

}
