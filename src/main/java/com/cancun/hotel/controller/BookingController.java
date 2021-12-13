package com.cancun.hotel.controller;

import com.cancun.hotel.domain.Booking;
import com.cancun.hotel.domain.BookingRequest;
import com.cancun.hotel.domain.BookingResponse;
import com.cancun.hotel.service.BookingService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cancun.hotel.utils.Messages.NO_BOOKING_FOUND_FOR_GIVEN_ID;

@RestController
@RequestMapping("/api/booking")
@ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server error")})
public class BookingController {

    private final BookingService service;

    @Autowired
    public BookingController(final BookingService service) {
        this.service = service;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval")})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<List<Booking>>> listAllBookings() {
        return new ResponseEntity<>(BookingResponse.of(service.findAllBookings()), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Booking not found"),
            @ApiResponse(code = 200, message = "Successful retrieval")})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<Booking>> findBookingById(@PathVariable String id) {
        Optional<Booking> booking = service.findBookingById(id);
        return booking
                .map(value -> new ResponseEntity<>(BookingResponse.of(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(BookingResponse.of(List.of(String.format(NO_BOOKING_FOUND_FOR_GIVEN_ID, id))), HttpStatus.NOT_FOUND));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval")})
    @GetMapping(value = "/booked", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<Set<String>>> listAllBookedDates() {
        Set<String> bookedDates = service.findAllBookedDates()
                .stream()
                .map(LocalDate::toString)
                .collect(Collectors.toSet());
        return new ResponseEntity<>(BookingResponse.of(bookedDates), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval")})
    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<Set<String>>> listAllAvailableDates() {
        Set<String> availableDates = service.findAllAvailableDates()
                .stream()
                .map(LocalDate::toString)
                .collect(Collectors.toSet());
        return new ResponseEntity<>(BookingResponse.of(availableDates), HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 201, message = "Created")})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<Booking>> createBooking(@RequestBody final BookingRequest request) {
        Booking booking = Booking.of(UUID.randomUUID().toString(), request.checkIn, request.checkOut);
        List<String> errors = service.validateBooking(booking);
        return errors.isEmpty() ?
                new ResponseEntity<>(BookingResponse.of(service.saveBooking(booking)), HttpStatus.CREATED) :
                new ResponseEntity<>(BookingResponse.of(errors), HttpStatus.BAD_REQUEST);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 201, message = "Created")})
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse<Booking>> updateBooking(@PathVariable String id, @RequestBody final BookingRequest request) {
        Booking booking = Booking.of(id, request.checkIn, request.checkOut);
        List<String> errors = service.validateBooking(booking);
        return errors.isEmpty() ?
                new ResponseEntity<>(BookingResponse.of(service.saveBooking(booking)), HttpStatus.CREATED) :
                new ResponseEntity<>(BookingResponse.of(errors), HttpStatus.BAD_REQUEST);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Created")})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable String id) {
        service.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
