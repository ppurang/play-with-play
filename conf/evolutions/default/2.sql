# --- First database schema

# --- !Ups

CREATE TABLE player (
  location                   VARCHAR(255) NOT NULL,
  hash                      VARCHAR(255) NOT NULL
);


ALTER TABLE player ADD CONSTRAINT uniqueLocation UNIQUE(location)

# --- !Downs

DROP TABLE IF EXISTS player;