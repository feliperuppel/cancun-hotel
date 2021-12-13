package com.cancun.hotel.utils;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

public interface DateUtils {
    LocalDate today();
    default LocalDate tomorrow(){
        return today().plusDays(1);
    }
    default LocalDate yesterday(){
        return today().minusDays(1);
    }
    default long countDays(LocalDate firstDayInclusive, LocalDate lastDayInclusive){
        LocalDate lastDayExclusive = lastDayInclusive.plusDays(1);
        return DAYS.between(firstDayInclusive, lastDayExclusive);
    }
    default Stream<LocalDate> streamOf(LocalDate firstDayInclusive, LocalDate lastDayInclusive){
        if(firstDayInclusive == null || lastDayInclusive == null){
            return Stream.empty();
        }
        LocalDate lastDayExclusive = lastDayInclusive.plusDays(1);
        return firstDayInclusive.datesUntil(lastDayExclusive);
    }
    default LocalDate countAndReturnDate(LocalDate initialDate, long dayCount){
        Assert.notNull(initialDate, "Initial Date must be not null");
        if(dayCount == 0){
            return initialDate;
        }
        long lastPosition = dayCount > 0 ? dayCount-1 : dayCount;
        return initialDate.plusDays(lastPosition);
    }
}
