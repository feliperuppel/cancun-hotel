package com.cancun.hotel.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class BookingRequest {
    public final LocalDate checkIn;
    public final LocalDate checkOut;

    @JsonCreator
    public BookingRequest(@JsonProperty("checkIn") final LocalDate checkIn, @JsonProperty("checkOut") final LocalDate checkOut){
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
}
