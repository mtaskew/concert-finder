package com.marquarius.aws;

import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class MessageCreator {
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    public static String createMessage(Concert concert, String filterState, String stateAbbreviation) {
        String message = "";
        StringBuilder stringBuilder = new StringBuilder();

        if(stateAbbreviation.isEmpty()) {
            stringBuilder.append("C'MON man... In what world is ")
                    .append(filterState)
                    .append(" a US state? I forgive you though. Just go catch the next ")
                    .append(concert.getArtistName())
                    .append(" concert in ")
                    .append(concert.getCity())
                    .append(" ")
                    .append(concert.getState())
                    .append(" on ")
                    .append(dateFormatter.format(concert.getDate()))
                    .append(" at ")
                    .append(timeFormatter.format(concert.getTime()));

            addTheVenueLocationToTheStringBuilder(concert, stringBuilder);

        } else {
            stringBuilder.append("You can go catch ")
                    .append(concert.getArtistName())
                    .append(" on ")
                    .append(dateFormatter.format(concert.getDate()))
                    .append(" in ")
                    .append(concert.getCity())
                    .append(" at ")
                    .append(timeFormatter.format(concert.getTime()));

            addTheVenueLocationToTheStringBuilder(concert, stringBuilder);
        }
        message = stringBuilder.toString();
        return message;
    }

    public static String createErrorMessage() {
        String message = "Sorry my contact is acting weird, and I can't find concerts at this time. :(";
        return message;
    }

    public static String createNonFoundMessage(String artistName, String filterState, String enteredState) {
        String message = "";
        StringBuilder stringBuilder = new StringBuilder();

        if(StringUtils.isNotBlank(filterState)) {
            stringBuilder.append("My people couldn't find any upcoming ")
                    .append(artistName)
                    .append(" concerts in ")
                    .append(filterState)
                    .append(". Don't let it stop you though. Go party with another artist.");
        } else {
            stringBuilder.append("We both know that ")
                    .append(enteredState)
                    .append(" is not a valid US state, so I ignored it. My people couldn't find any upcoming ")
                    .append(artistName)
                    .append(" concerts in the US though. :(");
        }
        message = stringBuilder.toString();
        return message;
    }

    private static void addTheVenueLocationToTheStringBuilder(Concert concert, StringBuilder stringBuilder) {
        if(StringUtils.isNotBlank(concert.getLocation())) {
            stringBuilder.append(" in the ")
                    .append(concert.getLocation())
                    .append(".");
        } else {
            stringBuilder.append(".");
        }
    }
}
