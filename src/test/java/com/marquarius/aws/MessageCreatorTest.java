package com.marquarius.aws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class MessageCreatorTest {
    private Concert concert;

    @Before
    public void setUp() {
        concert = createConcert();
    }

    @After
    public void tearDown() {
        concert = null;
    }

    @Test
    public void testCreateMessage_with_empty_abbreviation_state() {
        String message = MessageCreator.createMessage(concert, "AL", "");
        assertThat(message).contains("C'MON man...");
    }

    @Test
    public void testCreateMessage_with_valid_state_abbreviation() {
        String message = MessageCreator.createMessage(concert, "AL", "AL");
        assertThat(message).contains("You can go catch ");
    }

    @Test
    public void testCreateNonFoundMessage_with_empty_state_value_and_concert_location_is_null() {
        concert.setLocation(null);
        String message = MessageCreator.createNonFoundMessage("Roscoe", "", "kjkj");
        assertThat(message).contains("We both know that");
    }

    @Test
    public void testCreateNonFoundMessage_with_valid_state_value() {
        String message = MessageCreator.createNonFoundMessage("Roscoe", "AL", "AL");
        assertThat(message).contains("My people couldn't find any upcoming");
    }

    private Concert createConcert() {
        Concert concert = new Concert();
        concert.setState("AL");
        concert.setArtistName("Roscoe");
        concert.setLocation("Civic Center");
        concert.setDate(LocalDate.now());
        concert.setTime(LocalTime.now());
        concert.setCity("Dallas");
        return concert;
    }
}
