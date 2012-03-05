# --- First database schema

# --- !Ups

CREATE TABLE bar (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(255) NOT NULL
);

# --- !Downs

DROP TABLE IF EXISTS bar;