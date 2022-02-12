package com.hologramsciences;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;

import io.atlassian.fugue.Option;
import org.apache.commons.lang3.ObjectUtils;

public class CSVRestaurantService {
    private final List<Restaurant> restaurantList;

    /**
     * TODO: Implement Me
     * <p>
     * From the CSVRecord which represents a single line from src/main/resources/rest_hours.csv
     * Write a parser to read the line and create an instance of the Restaurant class (Optionally, using the Option class)
     * <p>
     * Example Line:
     * <p>
     * "Burger Bar","Mon,Tue,Wed,Thu,Sun|11:00-22:00;Fri,Sat|11:00-0:00"
     * <p>
     * '|'   separates the list of applicable days from the hours span
     * ';'   separates groups of (list of applicable days, hours span)
     * <p>
     * So the above line would be parsed as:
     * <p>
     * Map<DayOfWeek, OpenHours> m = new HashMap<>();
     * m.put(MONDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(TUESDAY,   new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(WEDNESDAY, new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(THURSDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(SUNDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * <p>
     * m.put(FRIDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     * m.put(SATURDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     * <p>
     * Option.some(new Restaurant("Burger Bar", m))
     * <p>
     * This method returns Option.some(parsedRestaurant),
     * IF the String name, and Map<DayOfWeek, OpenHours> openHours is found in the CSV,
     * - assume if both columns are in the CSV then they are both parsable.
     * AND if all values in openHours have !startTime.equals(endTime)
     * <p>
     * This method returns Option.none() when any of the OpenHours for a given restaurant have the same startTime and endDate
     * <p>
     * <p>
     * NOTE, the getDayOfWeek method should be helpful, and the LocalTime should be parsable by LocalDate.parse
     */
    public static Option<Restaurant> parse(final CSVRecord r) {
        if (r.size() > 1) {
            String name = r.get(0);
            String dayHourString = r.get(1);

            if (ObjectUtils.isNotEmpty(dayHourString)) {
                Map<DayOfWeek, Restaurant.OpenHours> openHours = parseOpenHour(dayHourString);
                for (DayOfWeek day : openHours.keySet()) {
                    Restaurant.OpenHours openHours1 = openHours.get(day);
                    if (openHours1.getStartTime() == openHours1.getEndTime())
                        Option.none();
                }
                Restaurant parsedRestaurant = new Restaurant(name, openHours);
                return Option.some(parsedRestaurant);
            }
        }
        return Option.none();
    }

    /**
     * TODO: Implement me, This is a useful helper method
     */
    public static Map<DayOfWeek, Restaurant.OpenHours> parseOpenHour(final String openhoursString) {
        Map<DayOfWeek, Restaurant.OpenHours> openHours = new HashMap<>();
        try {
            System.out.println("Given String :" + openhoursString + "\n");
            List<String> res = new ArrayList<>();
            for (String s : Arrays.asList(openhoursString.split(",")))
                if (s.contains(";"))
                    res.addAll(Arrays.asList(s.split(";")));
                else
                    res.add(s);
            int startHour = 0, startMinute = 0;
            int endHour = 0, endMinute = 0;
            for (int i = res.size() - 1; i >= 0; i--) {
                String day = res.get(i);
                if (day.length() == 3) {
                    openHours.put(getDayOfWeek(day).get(), new Restaurant.OpenHours(LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));
                } else {
                    String[] last = day.split("\\|");
                    String[] times = last[1].split("-");
                    String[] start = times[0].split(":");
                    String[] end = times[1].split(":");

                    startHour = Integer.parseInt(start[0]);
                    startMinute = Integer.parseInt(start[1]);

                    endHour = Integer.parseInt(end[0]);
                    endMinute = Integer.parseInt(end[1]);
                    openHours.put(getDayOfWeek(last[0]).get(), new Restaurant.OpenHours(LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));
                }
            }
            return openHours;
        } catch (Exception e) {
            System.out.println(e);
        }
        return openHours;
    }

    public CSVRestaurantService() throws IOException {
        this.restaurantList = ResourceLoader.parseOptionCSV("rest_hours.csv", CSVRestaurantService::parse);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantList;
    }

    /**
     * TODO: Implement me
     * <p>
     * A restaurant is considered open when the OpenHours for the dayOfWeek has:
     * <p>
     * startTime < localTime   && localTime < endTime
     * <p>
     * If the open hours are 16:00-20:00  Then
     * <p>
     * 15:59 open = false
     * 16:00 open = false
     * 16:01 open = true
     * 20:00 open = false
     * <p>
     * <p>
     * If the startTime endTime spans midnight, then consider an endTime up until 5:00 to be part of same DayOfWeek as the startTime
     * <p>
     * SATURDAY, OpenHours are: 20:00-04:00    SUNDAY, OpenHours are: 10:00-14:00
     * <p>
     * (SATURDAY, 03:00) => open = false
     * (SUNDAY, 03:00)   => open = true
     * (SUNDAY, 05:00)   => open = false
     */
    public List<Restaurant> getOpenRestaurants(final DayOfWeek dayOfWeek, final LocalTime localTime) {
        List<Restaurant> openRestaurants = new ArrayList<>();
        for (Restaurant restaurant : restaurantList) {
            Restaurant.OpenHours openHours = restaurant.getOpenHoursMap().get(dayOfWeek);
            if (localTime.isAfter(openHours.getStartTime()) && localTime.isBefore(openHours.getEndTime())) {
                openRestaurants.add(restaurant);
            }
        }

        return openRestaurants;
    }

    public List<Restaurant> getOpenRestaurantsForLocalDateTime(final LocalDateTime localDateTime) {
        return getOpenRestaurants(localDateTime.getDayOfWeek(), localDateTime.toLocalTime());
    }

    public static Option<DayOfWeek> getDayOfWeek(final String s) {

        if (s.equals("Mon")) {
            return Option.some(DayOfWeek.MONDAY);
        } else if (s.equals("Tue")) {
            return Option.some(DayOfWeek.TUESDAY);
        } else if (s.equals("Wed")) {
            return Option.some(DayOfWeek.WEDNESDAY);
        } else if (s.equals("Thu")) {
            return Option.some(DayOfWeek.THURSDAY);
        } else if (s.equals("Fri")) {
            return Option.some(DayOfWeek.FRIDAY);
        } else if (s.equals("Sat")) {
            return Option.some(DayOfWeek.SATURDAY);
        } else if (s.equals("Sun")) {
            return Option.some(DayOfWeek.SUNDAY);
        } else {
            return Option.none();
        }
    }

    public static <S, T> Function<S, Stream<T>> toStreamFunc(final Function<S, Option<T>> function) {
        return s -> function.apply(s).fold(() -> Stream.empty(), t -> Stream.of(t));
    }

    /**
     * NOTE: Useful for generating the data.sql file in src/main/resources/
     */
    public static void main(final String[] args) throws IOException {
        final CSVRestaurantService csvRestaurantService = new CSVRestaurantService();

        csvRestaurantService.getAllRestaurants().forEach(restaurant -> {

            final String name = restaurant.getName().replaceAll("'", "''");

            System.out.println("INSERT INTO restaurants (name) values ('" + name + "');");

            restaurant.getOpenHoursMap().entrySet().forEach(entry -> {
                final DayOfWeek dayOfWeek = entry.getKey();
                final LocalTime startTime = entry.getValue().getStartTime();
                final LocalTime endTime = entry.getValue().getEndTime();

                System.out.println("INSERT INTO open_hours (restaurant_id, day_of_week, start_time_minute_of_day, end_time_minute_of_day) select id, '" + dayOfWeek.toString() + "', " + startTime.get(ChronoField.MINUTE_OF_DAY) + ", " + endTime.get(ChronoField.MINUTE_OF_DAY) + " from restaurants where name = '" + name + "';");

            });
        });
    }

    static void printArray(String[] arr) {
        System.out.println();
        for (String s : arr)
            System.out.println(s);
    }
}
