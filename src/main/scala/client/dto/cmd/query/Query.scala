package client.dto.cmd.query

import client.dto.cmd._
import client.dto.cmd.query.list.{ListArg, ListParam}
import client.dto.cmd.query.meta.{MetaArg, MetaParam}
import client.dto.cmd.query.prop.{PropArg, PropParam}


/**
 * ?action=query
 *
 */

case class Query(override val params: QueryParam[Any]*)
  extends  EnumArgument[ActionArg]("query", "Various queries.")
  with ActionArg
  with ArgWithParams[QueryParam[Any], ActionArg] {

  val lists: Seq[ListArg] = byType(manifest[ListParam]).flatMap(_.args)
  val props: Seq[PropArg] = byType(manifest[PropParam]).flatMap(_.args)
  val metas: Seq[MetaArg] = byType(manifest[MetaParam]).flatMap(_.args)
}

/**
 * Marker trait for parameters available together with ?action=query
 *
 */
trait QueryParam[+T] extends Parameter[T]

import client.dto.Page.Id

case class TitlesParam(override val args: Seq[String])
  extends StringListParameter("titles", "A list of titles to work on") with QueryParam[String]
case class PageIdsParam(override val args: Seq[Id])
  extends IntListParameter("pageids", "A list of page IDs to work on") with QueryParam[Int]
case class RevIdsParam(override val args: Seq[Id])
  extends IntListParameter("revids", "A list of revision IDs to work on") with QueryParam[Int]

//object GeneratorParam extends GeneratorParameter("generator", "Get the list of pages to work on by executing the specified query module") with QueryParam[Int]


//indexpageids Include an additional pageids section listing all returned page IDs.
//export Export the current revisions of all given or generated pages.
//exportnowrap Return the export XML without wrapping it in an XML result (same format as Special:Export). Can only be used with export.
//iwurl Whether to get the full URL if the title is an interwiki link.
//continue When present, formats query-continue as key-value pairs that should simply be merged into the original request. This parameter must be set to an empty string in the initial query. This parameter is recommended for all new development, and will be made default in the next API version.
//rawcontinue Currently ignored. In the future, continue will become the default and this will be needed to receive the raw query-continue data.
//titles A list of titles to work on.
//pageids A list of page IDs to work on.
//revids A list of revision IDs to work on.
//generator Get the list of pages to work on by executing the specified query module. Note: Generator parameter names must be prefixed with a "g", see examples.
//redirects Automatically resolve redirects in titles, pageids, and revids, and in pages returned by generator.
//converttitles Convert titles to other variants if necessary. Only works if the wiki's content language supports variant conversion. Languages that support variant conversion include gan, iu, kk, ku, shi, sr, tg, uz, zh.