package com.cancun.hotel.service;

import com.cancun.hotel.domain.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingService {

    List<String> validateBooking(Booking booking);

    Booking saveBooking(Booking booking);

    List<Booking> findAllBookings();

    Optional<Booking> findBookingById(String id);

    Set<LocalDate> findAllBookedDates();

    Set<LocalDate> findAllAvailableDates();

    void cancelBooking(String id);

}
