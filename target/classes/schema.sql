CREATE TABLE IF NOT EXISTS restaurants (
    id    BIGSERIAL NOT NULL PRIMARY KEY,
    name  TEXT
);


CREATE TABLE IF NOT EXISTS open_hours (

    id                        BIGSERIAL NOT NULL PRIMARY KEY,
    restaurant_id             BIGINT NOT NULL REFERENCES restaurants (id),

    day_of_week               TEXT NOT NULL,
    start_time_minute_of_day  INTEGER NOT NULL,
    end_time_minute_of_day    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (

    id                        BIGSERIAL NOT NULL PRIMARY KEY,
    restaurant_id             BIGINT NOT NULL REFERENCES restaurants (id),

    name                      TEXT NOT NULL
);
