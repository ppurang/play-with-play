package models

import java.net.URI
import anorm.SqlParser._
import anorm.~._
import play.api.db.DB
import anorm._
import java.util.UUID
import play.api.Play.current
import org.h2.jdbc.JdbcSQLException
import org.codehaus.jackson.annotate.JsonIgnore


trait Player

trait RegisteredPlayer {
  def key: String
}

trait KeyGen {
  def gen: String
}

object UUIDKeyGen extends KeyGen {
  def gen = UUID.randomUUID().toString
}

case class UriPlayer(location: String) extends Player {
  @JsonIgnore lazy val uri = URI.create(location)
}

case class RegisteredUriPlayer(location: String, key: String) extends Player with RegisteredPlayer {
  @JsonIgnore lazy val uri = URI.create(location)
}

object RegisteredUriPlayer {
  val simple = {
    get[String]("location") ~
      get[String]("hash") map {
      case name ~ mash => RegisteredUriPlayer(name, mash) //if the pk doesn't exist we are screwed anyways
    }
  }

  def findAll(): Seq[RegisteredUriPlayer] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from player").as(RegisteredUriPlayer.simple *)
    }
  }

  def create(rp: RegisteredUriPlayer): Either[Throwable, Int] = {
    DB.withConnection {
      implicit connection =>
        try {
          Right(SQL("insert into player(location, hash) values ({location}, {hash})").on(
            'location -> rp.location,
            'hash -> rp.key
          ).executeUpdate())
        }
        catch {
          case e => Left(e)
        }
    }
  }
}