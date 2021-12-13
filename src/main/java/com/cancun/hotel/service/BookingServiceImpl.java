package com.cancun.hotel.service;

import com.cancun.hotel.domain.Booking;
import com.cancun.hotel.repository.BookingRepository;
import com.cancun.hotel.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cancun.hotel.utils.Messages.*;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final long latestDateInDays;
    private final long maxBookingPeriodInDays;
    final BookingRepository repository;
    final DateUtils dateUtils;

    @Autowired
    public BookingServiceImpl(
            @Value("${booking.latest-date-in-days}") final long latestDateInDays,
            @Value("${booking.max-period-in-days}") final long maxBookingPeriodInDays,
            final BookingRepository repository,
            final DateUtils dateUtils
    ) {
        this.latestDateInDays = latestDateInDays;
        this.maxBookingPeriodInDays = maxBookingPeriodInDays;
        this.repository = repository;
        this.dateUtils = dateUtils;
    }

    @Override
    public List<Booking> findAllBookings() {
        return repository.findAll();
    }

    @Override
    public Optional<Booking> findBookingById(final String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public void cancelBooking(final String id) {
        repository.deleteById(id);
    }

    @Override
    public List<String> validateBooking(final Booking booking) {
        List<String> errors = new ArrayList<>();

        LocalDate checkIn = booking.getCheckIn();
        LocalDate checkOut = booking.getCheckOut();

        Assert.notNull(checkIn, CHECK_IN_NOT_NULL);
        Assert.notNull(checkOut, CHECK_OUT_NOT_NULL);

        validateDate("CheckIn", checkIn, errors);
        validateDate("CheckOut", checkOut, errors);
        validatePeriod(checkIn, checkOut, errors);
        validateAvailability(booking, errors);

        if (!errors.isEmpty()) {
            log.info("Validation errors found for booking {}", booking);
            errors.forEach(log::info);
        }

        return errors;
    }

    private void validatePeriod(LocalDate checkIn, LocalDate checkOut, List<String> errors) {
        if (checkIn.isEqual(checkOut) || checkIn.isAfter(checkOut)) {
            errors.add(CHECK_IN_AFTER_CHECK_OUT);
        }
        if (dateUtils.countDays(checkIn, checkOut) > maxBookingPeriodInDays) {
            errors.add(String.format(STAY_TOO_LONG, maxBookingPeriodInDays));
        }
    }

    private void validateDate(String dateType, LocalDate date, List<String> errors) {
        final LocalDate tomorrow = dateUtils.tomorrow();
        if (date.isBefore(tomorrow)) {
            errors.add(String.format(TOO_EARLY, dateType, tomorrow));
        }

        LocalDate lastValidDate = dateUtils.countAndReturnDate(tomorrow, latestDateInDays);
        if (date.isAfter(lastValidDate)) {
            errors.add(String.format(TOO_LATE, dateType, lastValidDate));
        }
    }

    private void validateAvailability(Booking booking, List<String> errors) {
        LocalDate checkIn = booking.getCheckIn();
        LocalDate checkOut = booking.getCheckOut();

        Set<LocalDate> bookedDates = findAllBookedDates();

        Optional<Booking> existentBooking = repository.findById(booking.getId());
        existentBooking.ifPresent(old -> {
            Set<LocalDate> ownBookingDates = dateUtils.streamOf(old.getCheckIn(), old.getCheckOut()).collect(Collectors.toSet());
            bookedDates.removeAll(ownBookingDates);
        });

        Set<LocalDate> unavailableDates = dateUtils.streamOf(checkIn, checkOut)
                .filter(bookedDates::contains)
                .collect(Collectors.toSet());

        if (!unavailableDates.isEmpty()) {
            errors.add(ALREADY_BOOKED);
        }
    }

    @Override
    public Set<LocalDate> findAllBookedDates() {
        LocalDate lowerBoundary = dateUtils.tomorrow();
        LocalDate higherBoundary = dateUtils.countAndReturnDate(lowerBoundary, latestDateInDays);
        return repository.findAll()
                .stream()
                .flatMap(r -> dateUtils.streamOf(r.getCheckIn(), r.getCheckOut()))
                .filter(d -> d.isAfter(lowerBoundary) || d.isEqual(lowerBoundary))
                .filter(d -> d.isBefore(higherBoundary) || d.isEqual(higherBoundary))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<LocalDate> findAllAvailableDates() {
        LocalDate lowerBoundary = dateUtils.tomorrow();
        LocalDate higherBoundary = dateUtils.countAndReturnDate(lowerBoundary,latestDateInDays);

        Set<LocalDate> bookedDates = this.findAllBookedDates();

        return dateUtils.streamOf(lowerBoundary, higherBoundary)
                .filter(d -> !bookedDates.contains(d))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Booking saveBooking(Booking booking) {
        return repository.save(booking);
    }
}
