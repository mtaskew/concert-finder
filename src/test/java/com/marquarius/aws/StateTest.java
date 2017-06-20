package com.marquarius.aws;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class StateTest {

    @Test
    public void testDetermineStateAbbreviation_proposed_state_value_is_alabama() {
        String abbreviation = State.determineStateAbbreviation("alabama");
        assertThat(abbreviation).isEqualTo("AL");
    }

    @Test
    public void testDetermineStateAbbreviation_proposed_state_value_is_Florida() {
        String abbreviation = State.determineStateAbbreviation("Florida");
        assertThat(abbreviation).isEqualTo("FL");
    }

    @Test
    public void testDetermineStateAbbreviation_proposed_state_value_is_fl() {
        String abbreviation = State.determineStateAbbreviation("fl");
        assertThat(abbreviation).isEqualTo("FL");
    }

    @Test
    public void testDetermineStateAbbreviation_proposed_state_value_is_invalid() {
        String abbreviation = State.determineStateAbbreviation("invalid");
        assertThat(abbreviation).isEmpty();
    }

    @Test
    public void testDetermineStateAbbreviation_proposed_state_is_null() {
        String abbreviation = State.determineStateAbbreviation(null);
        assertThat(abbreviation).isEmpty();
    }
}
