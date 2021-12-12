package com.cancun.hotel.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Booking {

    @Id
    private String id;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Instant created;

    private Booking(){
    }

    private Booking(final String id, final LocalDate checkIn, final LocalDate checkOut){
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.created = Instant.now();
    }

    @JsonCreator
    public static Booking of(
            @JsonProperty("id") final String id,
            @JsonProperty("checkIn")final LocalDate checkIn,
            @JsonProperty("checkOut")final LocalDate checkOut){
        return new Booking(id, checkIn, checkOut);
    }

    public String getId() {
        return id;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public Instant getCreated() {
        return created;
    }
}
