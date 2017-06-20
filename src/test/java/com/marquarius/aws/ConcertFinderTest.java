package com.marquarius.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.marquarius.aws.request.CurrentIntent;
import com.marquarius.aws.request.Request;
import com.marquarius.aws.request.Slots;
import com.marquarius.aws.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class ConcertFinderTest {
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private ConcertFinder concertFinder;
    private Context context;
    private Request request;

    @Before
    public void setUp() {
        concertFinder = new ConcertFinder();
        ReflectionTestUtils.setField(concertFinder, "restTemplate", restTemplate);
        request = createRequest();
    }

    @After
    public void tearDown() {
        request = null;
    }

    @Test
    public void testHandleRequest_concert_state_is_the_word_() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String concertTime = localDateTime.plusDays(1).toString();
        Map<String, Object> concert = createConcertMap(concertTime, "The Fillmore");
        Map<String, Object> venueMap = (Map<String, Object>) concert.get("venue");
        venueMap.put("region", "FL");
        Map[] concerts = new Map[]{concert};

        Slots slots = new Slots();
        slots.setArtistName("Yo Gotti");
        slots.setState("Florida");
        request.getCurrentIntent().setSlots(slots);

        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).doesNotContain("Sorry, I didn't recognize");
    }

    @Test
    public void testHandleRequest_no_concerts_returned() {
        Map[] concerts = new HashMap[]{};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains("My people couldn't find any upcoming");
    }

    @Test
    public void testHandleRequest_one_concert_returned_that_is_in_the_future() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String concertTime = localDateTime.plusDays(1).toString();

        Map<String, Object> concert = createConcertMap(concertTime, "The Fillmore");
        Map[] concerts = new Map[]{concert};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains("You can go catch");
    }

    @Test
    public void testHandleRequest_one_concert_returned_that_was_in_the_past() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String concertDate = localDateTime.minusDays(1).toString();

        Map<String, Object> concert = createConcertMap(concertDate, "The Fillmore");
        Map[] concerts = new Map[]{concert};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains("My people couldn't find any upcoming");
    }

    @Test
    public void testHandleRequest_one_concert_returned_with_ticket_offer() {

    }

    @Test
    public void testHandleRequest_two_concerts_returned_and_both_are_in_the_future() {
        String expectedVenue = "BJCC";
        LocalDateTime localDateTime = LocalDateTime.now();
        String mostRecentConcertDate = localDateTime.plusDays(1).toString();
        String laterConcertDate = localDateTime.plusDays(2).toString();

        Map<String, Object> concert = createConcertMap(laterConcertDate, "The Fillmore");
        Map<String, Object> concert1 = createConcertMap(mostRecentConcertDate, expectedVenue);
        Map[] concerts = new Map[]{concert1, concert};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains(expectedVenue);
    }

    @Test
    public void testHandleRequest_two_concerts_returned_and_both_are_in_the_future_on_the_same_day_at_diferent_times() {
        String expectedVenue = "BJCC";
        LocalDateTime localDateTime = LocalDateTime.now();
        String laterConcertDate = localDateTime.plusDays(1).toString();
        localDateTime = localDateTime.minusHours(1);
        String mostRecentConcertDate = localDateTime.plusDays(1).toString();


        Map<String, Object> concert = createConcertMap(laterConcertDate, "The Fillmore");
        Map<String, Object> concert1 = createConcertMap(mostRecentConcertDate, expectedVenue);
        Map[] concerts = new Map[]{concert, concert1};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains(expectedVenue);
    }

    @Test
    public void testHandleRequest_two_concerts_returned_one_was_in_the_past_and_one_is_in_the_future() {
        String expectedVenue = "The Fillmore";
        LocalDateTime localDateTime = LocalDateTime.now();
        String pastConcertDate = localDateTime.minusDays(1).toString();
        String futureConcertDate = localDateTime.plusDays(2).toString();

        Map<String, Object> concert = createConcertMap(futureConcertDate, expectedVenue);
        Map<String, Object> concert1 = createConcertMap(pastConcertDate, "BJCC");
        Map[] concerts = new Map[]{concert1, concert};
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(concerts));

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains(expectedVenue);
    }

    @Test
    public void testHandleRequest_rest_api_returns_404() {
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.notFound().build());

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains("Sorry my contact is acting weird");
    }

    @Test
    public void testHandleRequest_rest_call_throws_exception() {
        when(restTemplate.getForEntity(anyString(), any())).thenThrow(RuntimeException.class);

        Response response = concertFinder.handleRequest(request, context);
        assertThat(response.getDialogAction().getMessage().getContent()).contains("Sorry my contact is acting weird");
    }

    private Map<String, Object> createConcertMap(String date, String venueName) {
        Map<String, Object> concertMap = new HashMap<>();
        Map<String, Object> venueMap = new HashMap<>();
        venueMap.put("name", venueName);
        venueMap.put("region", "AL");
        venueMap.put("country", "United States");
        concertMap.put("venue", venueMap);
        concertMap.put("datetime", date);
        return concertMap;
    }

    private Request createRequest() {
        Request request = new Request();
        CurrentIntent currentIntent = new CurrentIntent();
        Slots slots = new Slots();
        slots.setArtistName("Yogi");
        slots.setState("AL");
        currentIntent.setSlots(slots);
        request.setCurrentIntent(currentIntent);
        return request;
    }
}
