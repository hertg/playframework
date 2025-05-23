/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.db

import jakarta.inject._
import play.api.test._
import play.api.Environment

class ConnectionPoolConfigSpec extends PlaySpecification {
  "DBModule bindings" should {
    "use HikariCP as default pool" in new WithApplication(
      _.configure(
        "db.default.url"  -> "jdbc:h2:mem:default",
        "db.other.driver" -> "org.h2.Driver",
        "db.other.url"    -> "jdbc:h2:mem:other"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi].database("default")
        db must beLike {
          case pdb: PooledDatabase => pdb.pool must haveClass[HikariCPConnectionPool]
        }
      }
    }

    "use HikariCP when default pool set to 'hikaricp'" in new WithApplication(
      _.configure(
        "play.db.pool"    -> "hikaricp",
        "db.default.url"  -> "jdbc:h2:mem:default",
        "db.other.driver" -> "org.h2.Driver",
        "db.other.url"    -> "jdbc:h2:mem:other"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi].database("default")
        db must beLike {
          case pdb: PooledDatabase => pdb.pool must haveClass[HikariCPConnectionPool]
        }
      }
    }

    "use custom class when default pool set to class name" in new WithApplication(
      _.configure(
        "play.db.pool"    -> classOf[CustomConnectionPool].getName,
        "db.default.url"  -> "jdbc:h2:mem:default",
        "db.other.driver" -> "org.h2.Driver",
        "db.other.url"    -> "jdbc:h2:mem:other"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi].database("default")
        db must beLike {
          case pdb: PooledDatabase => pdb.pool must haveClass[CustomConnectionPool]
        }
      }
    }

    "use custom class when database pool set to class name" in new WithApplication(
      _.configure(
        "db.default.pool" -> classOf[CustomConnectionPool].getName,
        "db.default.url"  -> "jdbc:h2:mem:default",
        "db.other.driver" -> "org.h2.Driver",
        "db.other.url"    -> "jdbc:h2:mem:other"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi].database("default")
        db must beLike {
          case pdb: PooledDatabase => pdb.pool must haveClass[CustomConnectionPool]
        }
      }
    }

    "do not use ConnectionPoolDataSourceProxy by default" in new WithApplication(
      _.configure(
        "db.default.driver" -> "org.h2.Driver",
        "db.default.url"    -> "jdbc:h2:mem:default"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi]
        db.database("default").dataSource.getClass.getName must not contain "ConnectionPoolDataSourceProxy"
      }
    }

    "use ConnectionPoolDataSourceProxy when logSql is true" in new WithApplication(
      _.configure(
        "db.default.driver" -> "org.h2.Driver",
        "db.default.url"    -> "jdbc:h2:mem:default",
        "db.default.logSql" -> "true"
      )
    ) {
      override def running() = {
        val db = app.injector.instanceOf[DBApi]
        db.database("default").dataSource.getClass.getName must contain("ConnectionPoolDataSourceProxy")
      }
    }
  }
}

@Singleton
class CustomConnectionPool @Inject() (environment: Environment) extends HikariCPConnectionPool(environment)
