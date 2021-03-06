package filodb.cassandra.columnstore

import scala.concurrent.Future

import com.typesafe.scalalogging.StrictLogging

import filodb.cassandra.FiloCassandraConnector
import filodb.core._

trait BaseDatasetTable extends StrictLogging {
  // The suffix for the dataset table, ie chunks, index, filter, etc.
  def dataset: DatasetRef
  def suffix: String
  lazy val keyspace = dataset.database.getOrElse(connector.defaultKeySpace)
  lazy val tableString = s"${keyspace}.${dataset.dataset + s"_$suffix"}"
  lazy val session = connector.session

  // A Cassandra CQL string to create the table.  Should have IF NOT EXISTS.
  def createCql: String
  def connector: FiloCassandraConnector

  def initialize(): Future[Response] = connector.execCql(createCql)

  def clearAll(): Future[Response] = connector.execCql(s"TRUNCATE $tableString")

  def drop(): Future[Response] = connector.execCql(s"DROP TABLE IF EXISTS $tableString")

  protected val sstableCompression = connector.config.getString("sstable-compression")
}