package com.cancun.hotel.service;

import com.cancun.hotel.domain.Booking;
import com.cancun.hotel.repository.BookingRepository;
import com.cancun.hotel.utils.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cancun.hotel.utils.Messages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    private static final long latestDateInDays = 30;
    private static final long maxBookingPeriodInDays = 3;
    private static BookingService service;
    private static BookingRepository repository;
    private static DateUtils dateUtils;
    private static LocalDate firstValidDate;
    private static LocalDate lastValidDate;

    @BeforeAll
    public static void init(){
        repository = Mockito.mock(BookingRepository.class);
        dateUtils = () -> LocalDate.of(2021, 12, 25);

        service = new BookingServiceImpl(
                latestDateInDays,
                maxBookingPeriodInDays,
                repository,
                dateUtils
        );

        firstValidDate = dateUtils.tomorrow();
        lastValidDate = dateUtils.countAndReturnDate(firstValidDate, latestDateInDays);
    }

    @AfterEach
    public void reset(){
        Mockito.reset(repository);
    }

    @Test
    public void validateBookingShouldReturnNoErrors(){
        Booking validBooking = getValidBooking();
        List<String> errors = service.validateBooking(validBooking);
        assertThat(errors).isEmpty();
    }

    @Test
    public void validateBookingShouldReturnErrorWhenPastCheckIn(){
        LocalDate checkIn = dateUtils.today();
        Booking invalidBooking = Booking.of("mock", checkIn, getDefaultCheckout(checkIn));
        String expectedError = String.format(TOO_EARLY, "CheckIn", dateUtils.tomorrow());

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(expectedError);
    }

    @Test
    public void validateBookingShouldReturnErrorWhenCheckInTooLate(){
        LocalDate firstInvalidDate = lastValidDate.plusDays(1);
        Booking invalidBooking = Booking.of("mock", firstInvalidDate, getDefaultCheckout(firstInvalidDate));
        String expectedError = String.format(TOO_LATE, "CheckIn", lastValidDate);

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors.get(0)).contains(expectedError);
    }

    @Test
    public void validateBookingShouldThrowExceptionWhenNullCheckIn(){
        assertThrows(IllegalArgumentException.class, () -> service.validateBooking(Booking.of("mock", null, dateUtils.tomorrow())));
    }

    @Test
    public void validateBookingShouldReturnErrorWhenPastCheckOut(){
        LocalDate checkIn = dateUtils.today().minusDays(maxBookingPeriodInDays);
        LocalDate checkOut = dateUtils.yesterday();
        Booking invalidBooking = Booking.of("mock", checkIn, checkOut);
        String expectedError = String.format(TOO_EARLY, "CheckOut", dateUtils.tomorrow());

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(expectedError);
    }

    @Test
    public void validateBookingShouldReturnErrorWhenCheckOutTooLate(){
        LocalDate checkOut = getDefaultCheckout(lastValidDate);
        Booking invalidBooking = Booking.of("mock", lastValidDate, checkOut);
        String expectedError = String.format(TOO_LATE, "CheckOut", lastValidDate);

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(expectedError);
    }

    @Test
    public void validateBookingShouldThrowExceptionWhenNullCheckOut(){
        assertThrows(IllegalArgumentException.class, () -> service.validateBooking(Booking.of("mock", dateUtils.tomorrow(), null)));
    }

    @Test
    public void validateBookingShouldReturnErrorWhenCheckInIsAfterCheckOut(){
        LocalDate checkIn = dateUtils.tomorrow();
        LocalDate checkOut = dateUtils.tomorrow();
        Booking invalidBooking = Booking.of("mock", checkIn, checkOut);

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(CHECK_IN_AFTER_CHECK_OUT);
    }

    @Test
    public void validateBookingShouldReturnErrorWhenStayIsLongerThanMaxAllowedPeriod(){
        LocalDate checkIn = dateUtils.tomorrow();
        LocalDate checkOut = checkIn.plusDays(maxBookingPeriodInDays);
        Booking invalidBooking = Booking.of("mock", checkIn, checkOut);
        String expectedError = String.format(STAY_TOO_LONG, maxBookingPeriodInDays);

        List<String> errors = service.validateBooking(invalidBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(expectedError);
    }

    @Test
    public void validateBookingShouldReturnErrorWhenCheckInIsNotAvailable(){
        LocalDate tomorrow = dateUtils.tomorrow();
        Booking preBooked = Booking.of("preBooked", tomorrow.plusDays(1), tomorrow.plusDays(2));

        LocalDate checkIn = tomorrow.plusDays(2);
        LocalDate checkOut = getDefaultCheckout(checkIn);
        Booking newBooking = Booking.of("newBooking", checkIn, checkOut);

        when(repository.findAll()).thenReturn(List.of(preBooked));

        List<String> errors = service.validateBooking(newBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(ALREADY_BOOKED);
    }

    @Test
    public void validateBookingShouldReturnErrorWhenCheckOutIsNotAvailable(){
        LocalDate tomorrow = dateUtils.tomorrow();
        Booking preBooked = Booking.of("preBooked", tomorrow.plusDays(1), tomorrow.plusDays(2));
        Booking newBooking = Booking.of("newBooking", tomorrow, tomorrow.plusDays(1));

        when(repository.findAll()).thenReturn(List.of(preBooked));

        List<String> errors = service.validateBooking(newBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(ALREADY_BOOKED);
    }

    @Test
    public void validateBookingShouldReturnNoErrorsWhenUpdatingBooking(){
        LocalDate tomorrow = dateUtils.tomorrow();
        Booking preBooked = Booking.of("preBooked", tomorrow, tomorrow.plusDays(1));
        Booking newBooking = Booking.of("preBooked", tomorrow.plusDays(1), tomorrow.plusDays(2));
        Booking otherBooking = Booking.of("otherBooking", tomorrow.plusDays(latestDateInDays).minusDays(2), tomorrow.plusDays(latestDateInDays).minusDays(1));

        when(repository.findAll()).thenReturn(List.of(preBooked, otherBooking));
        when(repository.findById("preBooked")).thenReturn(Optional.of(preBooked));

        List<String> errors = service.validateBooking(newBooking);

        assertThat(errors).isEmpty();
    }

    @Test
    public void validateBookingShouldReturnErrorWhenUpdatingBooking(){
        LocalDate tomorrow = dateUtils.tomorrow();
        Booking preBooked = Booking.of("preBooked", tomorrow, tomorrow.plusDays(1));
        Booking newBooking = Booking.of("preBooked", tomorrow.plusDays(1), tomorrow.plusDays(2));
        Booking otherBooking = Booking.of("otherBooking", tomorrow.plusDays(2), tomorrow.plusDays(3));

        when(repository.findAll()).thenReturn(List.of(preBooked, otherBooking));
        when(repository.findById("preBooked")).thenReturn(Optional.of(preBooked));

        List<String> errors = service.validateBooking(newBooking);

        assertThat(errors).hasAtLeastOneElementOfType(String.class);
        assertThat(errors).contains(ALREADY_BOOKED);
    }

    @Test
    public void saveBookingShouldCallRepository(){
        Booking booking = getValidBooking();

        service.saveBooking(booking);

        ArgumentCaptor<Booking> argument = ArgumentCaptor.forClass(Booking.class);
        verify(repository, times(1)).save(any());
        verify(repository).save(argument.capture());
        assertThat(argument.getValue()).isEqualTo(booking);
    }

    @Test
    public void deleteBookingShouldCallRepository(){
        String bookingId = "mocked";

        service.cancelBooking(bookingId);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(repository, times(1)).deleteById(any());
        verify(repository).deleteById(argument.capture());
        assertThat(argument.getValue()).isEqualTo(bookingId);
    }

    @Test
    public void findAllShouldCallRepository(){
        List<Booking> bookings = List.of(
                getValidBooking("R1"),
                getValidBooking("R2"),
                getValidBooking("R3")
        );

        when(repository.findAll()).thenReturn(bookings);

        List<Booking> response = service.findAllBookings();

        assertThat(response).hasSize(3);
    }

    @Test
    public void findByIdShouldCallRepository(){
        String bookingId = "mocked";
        Booking mockedBooking = getValidBooking(bookingId);

        Mockito.when(repository.findById(bookingId)).thenReturn(Optional.of(mockedBooking));

        Optional<Booking> booking = service.findBookingById(bookingId);

        assertThat(booking).isPresent();
        assertThat(booking.get()).isEqualTo(mockedBooking);
    }

    @Test
    public void findAllBookedDatesShouldReturnAllDates(){
        List<Booking> bookings = List.of(
                Booking.of("R1", firstValidDate, firstValidDate.plusDays(2)),
                Booking.of("R2", lastValidDate.minusDays(2), lastValidDate)
        );

        when(repository.findAll()).thenReturn(bookings);

        Set<LocalDate> bookedDates = service.findAllBookedDates();

        assertThat(bookedDates).hasSize(6);
        assertThat(bookedDates).contains(
                firstValidDate,
                firstValidDate.plusDays(1),
                firstValidDate.plusDays(2),
                lastValidDate,
                lastValidDate.minusDays(1),
                lastValidDate.minusDays(2)
        );
    }

    @Test
    public void findAllBookedDatesShouldReturnEmptySet(){
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Set<LocalDate> bookedDates = service.findAllBookedDates();

        assertThat(bookedDates).isEmpty();
    }

    @Test
    public void findAllAvailableDatesShouldReturnAllDates(){
        List<Booking> bookings = List.of(
                Booking.of("R1", firstValidDate.plusDays(3), lastValidDate.minusDays(3))
        );

        when(repository.findAll()).thenReturn(bookings);

        Set<LocalDate> availableDates = service.findAllAvailableDates();

        assertThat(availableDates).hasSize(6);
        assertThat(availableDates).contains(
                firstValidDate,
                firstValidDate.plusDays(1),
                firstValidDate.plusDays(2),
                lastValidDate,
                lastValidDate.minusDays(1),
                lastValidDate.minusDays(2)
        );
    }

    @Test
    public void findAllAvailableDatesShouldReturnEmptySet(){
        when(repository.findAll())
                .thenReturn(List.of(
                        Booking.of(
                                "mock",
                                dateUtils.tomorrow(),
                                dateUtils.tomorrow().plusDays(latestDateInDays))));

        Set<LocalDate> availableDates = service.findAllAvailableDates();

        assertThat(availableDates).isEmpty();
    }

    private static Booking getValidBooking(){
        return getValidBooking(String.format("RandomId-%s", Math.random()));
    }

    private static Booking getValidBooking(String id){
        LocalDate checkIn = dateUtils.tomorrow();
        return Booking.of(id, checkIn, getDefaultCheckout(checkIn));
    }

    private static LocalDate getDefaultCheckout(LocalDate checkIn){
        return dateUtils.countAndReturnDate(checkIn, maxBookingPeriodInDays);
    }

}
