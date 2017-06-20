package com.marquarius.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.marquarius.aws.request.CurrentIntent;
import com.marquarius.aws.request.Request;
import com.marquarius.aws.request.Slots;
import com.marquarius.aws.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class ConcertFinder implements RequestHandler<Request, Response> {
    private RestTemplate restTemplate = new RestTemplate();
    private Logger logger = LoggerFactory.getLogger(ConcertFinder.class);
    private static final String VENUE_KEY = "venue";
    private static final String STATE_KEY = "region";
    private static final String DATE_TIME_KEY = "datetime";

    @Override
    public Response handleRequest(Request request, Context context) {
        CurrentIntent currentIntent = request.getCurrentIntent();
        String message = "";

        if(currentIntent != null && currentIntent.getSlots() != null
                && currentIntent.getSlots().getArtistName() != null) {
            Slots slots = currentIntent.getSlots();
            String url = buildRequestUrl(slots.getArtistName());
            String state = slots.getState();
            String artistName = slots.getArtistName();

            logger.info("Finding concert. artistName=" + artistName + " state=" + state);

            try {
                ResponseEntity<Map[]> responseEntity = restTemplate.getForEntity(url, Map[].class);
                if(responseEntity != null && responseEntity.getStatusCode() != null
                        && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    String stateAbbreviation = State.determineStateAbbreviation(state);
                    Map[] returnedConcerts = responseEntity.getBody();
                    if(returnedConcerts != null && returnedConcerts.length > 0) {
                        Optional<Concert> nextConcert = Arrays.stream(returnedConcerts)
                                .filter(concertMap -> {
                                    Map<String, Object> venueMap = (Map<String, Object>) concertMap.get(VENUE_KEY);
                                    String country = (String) venueMap.getOrDefault("country", "");
                                    return "United States".equalsIgnoreCase(country);
                                })
                                .filter(concertMap -> {
                                    if(!stateAbbreviation.isEmpty()) {
                                        Map<String, Object> venueMap = (Map<String, Object>) concertMap.get(VENUE_KEY);
                                        String region = (String) venueMap.getOrDefault(STATE_KEY, "");
                                        return stateAbbreviation.equalsIgnoreCase(region);
                                    } else {
                                        return true;
                                    }
                                })
                                .filter(concertMap -> {
                                    boolean isValidDate = false;
                                    LocalDate today = LocalDate.now();
                                    LocalDate yesterday = today.minusDays(1);
                                    String localDateTimeString = (String) concertMap.get(DATE_TIME_KEY);

                                    if(localDateTimeString != null && !localDateTimeString.isEmpty()) {
                                        try {
                                            LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString);
                                            isValidDate = localDateTime.toLocalDate().isAfter(yesterday);
                                        } catch(Exception e) {

                                        }
                                    }
                                    return isValidDate;
                                })
                                .sorted((m1, m2) -> {
                                    String m1LocalDateTimeString = (String) m1.get(DATE_TIME_KEY);
                                    LocalDateTime m1LocalDateTime = LocalDateTime.parse(m1LocalDateTimeString);
                                    String m2LocalDateTimeString = (String) m2.get(DATE_TIME_KEY);
                                    LocalDateTime m2LocalDateTime = LocalDateTime.parse(m2LocalDateTimeString);
                                    return m1LocalDateTime.compareTo(m2LocalDateTime);
                                })
                                .map(concertMap -> {
                                    Map<String, Object> venueMap = (Map<String, Object>) concertMap.get(VENUE_KEY);
                                    String location = (String) venueMap.getOrDefault("name", "Unknown Venue");
                                    String localDateTimeString = (String) concertMap.get(DATE_TIME_KEY);
                                    LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString);
                                    List<Map<String, Object>> offersMap = (List<Map<String, Object>>) concertMap.get("offers");
                                    String ticketLink = extractTicketLink(offersMap);
                                    String region = (String) venueMap.getOrDefault(STATE_KEY, "");
                                    String city = (String) venueMap.get("city");

                                    Concert concert = new Concert();
                                    concert.setArtistName(artistName);
                                    concert.setDate(localDateTime.toLocalDate());
                                    concert.setTime(localDateTime.toLocalTime());
                                    concert.setLocation(location);
                                    concert.setState(region);
                                    concert.setCity(city);
                                    concert.setTicketLink(ticketLink);
                                    return concert;
                                })
                                .findFirst();

                        if(nextConcert.isPresent()) {
                            message = MessageCreator.createMessage(nextConcert.get(), state, stateAbbreviation);

                        } else {
                            message = MessageCreator.createNonFoundMessage(artistName, stateAbbreviation, state);
                            //TODO: Another response type
                            //TODO:

                        }
                    } else {
                        message = MessageCreator.createNonFoundMessage(artistName, stateAbbreviation, state);
                    }
                } else {
                    message = MessageCreator.createErrorMessage();
                }
            } catch(Exception e) {
                logger.error("Unable to find concerts.", e);
                message = MessageCreator.createErrorMessage();
            }

        } else {
            logger.error("Unrecognized request.");
            message = MessageCreator.createErrorMessage();
        }

        Response response = Response.generateFulfilledResponse(message);
        return response;
    }

    private String buildRequestUrl(String artistName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://rest.bandsintown.com/artists/")
                .append(artistName)
                .append("/events?app_id=concertBot");

        return stringBuilder.toString();
    }

    private String extractTicketLink(List<Map<String, Object>> offers) {
        String ticketLink = "";
        if(offers != null) {
            for(Map<String, Object> offer : offers) {
                String type = (String) offer.get("Type");
                String status = (String) offer.get("status");
                if(type != null && "Tickets".equalsIgnoreCase(type) && status != null
                        && "available".equalsIgnoreCase(status)) {
                    String link = (String) offer.get("url");
                    if(link != null && !link.isEmpty()) {
                        ticketLink = link;
                    }

                }
            }

        }
        return ticketLink;
    }
}
