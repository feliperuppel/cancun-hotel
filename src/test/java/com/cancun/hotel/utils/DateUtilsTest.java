package com.cancun.hotel.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateUtilsTest {

    private final DateUtils dateUtils = () -> LocalDate.EPOCH;

    @Test
    public void shouldReturnCountOf3() {
        LocalDate firstDate = dateUtils.yesterday();
        LocalDate lastDate = dateUtils.tomorrow();

        long daysCount = dateUtils.countDays(firstDate, lastDate);

        assertThat(daysCount).isEqualTo(3);
    }

    @Test
    public void shouldReturn3Dates() {
        LocalDate firstDate = dateUtils.yesterday();
        LocalDate lastDate = dateUtils.tomorrow();

        List<LocalDate> dates = dateUtils.streamOf(firstDate, lastDate).collect(Collectors.toList());

        assertThat(dates).hasSize(3);
        assertThat(dates).contains(dateUtils.yesterday());
        assertThat(dates).contains(dateUtils.today());
        assertThat(dates).contains(dateUtils.tomorrow());
    }

    @Test
    public void shouldReturnSameDate() {
        LocalDate today = dateUtils.today();
        LocalDate countedDate = dateUtils.countAndReturnDate(today, 1);

        assertThat(today).isEqualTo(countedDate);
    }

    @Test
    public void shouldReturnTomorrowDate() {
        LocalDate today = dateUtils.yesterday();
        LocalDate tomorrow = dateUtils.tomorrow();
        LocalDate countedDate = dateUtils.countAndReturnDate(today, 3);

        assertThat(countedDate).isEqualTo(tomorrow);
    }

    @Test
    public void shouldReturnJustOneDateIfBothParamsAreTheSame() {
        LocalDate aDate = dateUtils.today();
        List<LocalDate> dates = dateUtils.streamOf(aDate, aDate).collect(Collectors.toList());
        assertThat(dates).hasSize(1);
        assertThat(dates).contains(aDate);
    }

    @Test
    public void shouldReturnEmptyStreamWhenNegativeInterval() {
        LocalDate today = dateUtils.today();
        LocalDate tomorrow = dateUtils.tomorrow();
        Stream<LocalDate> stream = dateUtils.streamOf(tomorrow, today);
        assertThat(stream).isEmpty();
    }

    @Test
    public void shouldReturnEmptyStreamWhenInitialDateIsNull() {
        LocalDate today = dateUtils.today();
        Stream<LocalDate> stream = dateUtils.streamOf(null, today);
        assertThat(stream).isEmpty();
    }

    @Test
    public void shouldReturnEmptyStreamWhenFinalDateIsNull() {
        LocalDate tomorrow = dateUtils.tomorrow();
        Stream<LocalDate> stream = dateUtils.streamOf(tomorrow, null);
        assertThat(stream).isEmpty();
    }

    @Test
    public void shouldReturnSameDateIfDaysToCountIs0() {
        LocalDate today = dateUtils.today();
        LocalDate date = dateUtils.countAndReturnDate(today, 0);
        assertThat(date).isEqualTo(today);
    }

    @Test
    public void shouldCountBackwardsIfDaysToCountIsNegative() {
        LocalDate today = dateUtils.today();
        LocalDate yesterday = dateUtils.yesterday();
        LocalDate date = dateUtils.countAndReturnDate(today, -1);
        assertThat(date).isEqualTo(yesterday);
    }

    @Test
    public void shouldThrowExceptionIfInitialDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> dateUtils.countAndReturnDate(null, 1));
    }

}
