package com.sksamuel.elastic4s.searches

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.script.ScriptFieldDsl
import org.elasticsearch.action.search._
import org.elasticsearch.client.Client

import scala.concurrent.Future
import scala.language.implicitConversions

trait SearchDsl
  extends QueryDsl
    with HighlightDsl
    with ScriptFieldDsl {

  def search(indexes: Indexes): SearchDefinition = search(indexes.toIndexesAndTypes)
  def search(indexTypes: IndexAndTypes): SearchDefinition = search(indexTypes.toIndexesAndTypes)
  def search(indexesAndTypes: IndexesAndTypes): SearchDefinition = SearchDefinition(indexesAndTypes)

  @deprecated("please use typed version which allows better checking at compile time." +
    "eg searchIn(\"index\" / \"type\"), or searchIn(\"index1,index2\" / \"type1,type2\")", "5.0.0")
  def search(indexes: String*): SearchDefinition = SearchDefinition(IndexesAndTypes(indexes))

  def rescore(query: QueryDefinition) = RescoreDefinition(query)

  def multi(searches: Iterable[SearchDefinition]): MultiSearchDefinition = MultiSearchDefinition(searches)
  def multi(searches: SearchDefinition*): MultiSearchDefinition = MultiSearchDefinition(searches)

  implicit def toRichResponse(resp: SearchResponse): RichSearchResponse = RichSearchResponse(resp)

  implicit object SearchDefinitionExecutable
    extends Executable[SearchDefinition, SearchResponse, RichSearchResponse] {
    override def apply(c: Client, t: SearchDefinition): Future[RichSearchResponse] = {
      injectFutureAndMap(c.search(t.build, _))(RichSearchResponse.apply)
    }
  }

  implicit object MultiSearchDefinitionExecutable
    extends Executable[MultiSearchDefinition, MultiSearchResponse, MultiSearchResult] {
    override def apply(c: Client, t: MultiSearchDefinition): Future[MultiSearchResult] = {
      injectFutureAndMap(c.multiSearch(t.build, _))(MultiSearchResult.apply)
    }
  }

  implicit object SearchDefinitionShow extends Show[SearchDefinition] {
    override def show(f: SearchDefinition): String = f._builder.toString
  }

  implicit class SearchDefinitionShowOps(f: SearchDefinition) {
    def show: String = SearchDefinitionShow.show(f)
  }

  implicit object MultiSearchDefinitionShow extends Show[MultiSearchDefinition] {

    import compat.Platform.EOL

    override def show(f: MultiSearchDefinition): String = f.searches.map(_.show).mkString("[" + EOL, "," + EOL, "]")
  }

  implicit class MultiSearchDefinitionShowOps(f: MultiSearchDefinition) {
    def show: String = MultiSearchDefinitionShow.show(f)
  }
}
