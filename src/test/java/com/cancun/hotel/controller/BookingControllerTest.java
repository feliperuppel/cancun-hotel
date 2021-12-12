package com.cancun.hotel.controller;

import com.cancun.hotel.domain.Booking;
import com.cancun.hotel.domain.BookingRequest;
import com.cancun.hotel.domain.BookingResponse;
import com.cancun.hotel.service.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookingService service;

    private static final ObjectMapper mapper = getObjectMapper();
    private static final String BASE_URI = "/api/booking";
    private static final LocalDate MOCK_DATE = LocalDate.of(2021,12,25);

    @Test
    public void putShouldReturnStatus201AndBookingBody() throws JsonProcessingException {

        final BookingRequest mockedBooking = getBookingRequestMock();
        final String json = mapper.writeValueAsString(mockedBooking);

        when(service.validateBooking(any())).thenReturn(Collections.emptyList());
        when(service.saveBooking(any())).thenReturn(getBookingMock());

        ResponseEntity<BookingResponse<Booking>> response = restTemplate.exchange(createPutRequest(BASE_URI + "/abc", json), new ParameterizedTypeReference<>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors).isEmpty();
        assertThat(response.getBody().data).isNotNull();
    }

    @Test
    public void putShouldReturnStatus400AndListOfErrors() throws JsonProcessingException {

        String json = mapper.writeValueAsString(getBookingRequestMock());

        when(service.validateBooking(any())).thenReturn(List.of("Error1", "Error2"));

        ResponseEntity<BookingResponse<Booking>> response = restTemplate.exchange(createPutRequest(BASE_URI + "/abc", json), new ParameterizedTypeReference<>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isNull();
        assertThat(response.getBody().errors).hasSize(2);
    }

    @Test
    public void putShouldReturnStatus400WhenWrongJSON() {

        final String json = "{Invalid Json}";

        ResponseEntity<String> response = restTemplate.exchange(createPutRequest(BASE_URI + "/abc", json), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void shouldReturnStatus500() throws JsonProcessingException {

        final String exceptionMessage = "Mocked Exception";
        String json = mapper.writeValueAsString(getBookingRequestMock());

        when(service.validateBooking(any())).thenThrow(new RuntimeException(exceptionMessage));

        ResponseEntity<String> response = restTemplate.exchange(createPutRequest(BASE_URI + "/abc", json), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(exceptionMessage);
    }

    @Test
    public void postShouldReturnStatus201AndBookingBody() throws JsonProcessingException {

        final BookingRequest request = getBookingRequestMock();
        final String json = mapper.writeValueAsString(request);

        when(service.validateBooking(any())).thenReturn(Collections.emptyList());
        when(service.saveBooking(any())).thenReturn(getBookingMock());

        ResponseEntity<BookingResponse<Booking>> response = restTemplate.exchange(createPostRequest(BASE_URI, json), new ParameterizedTypeReference<>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors).isEmpty();
        assertThat(response.getBody().data).isNotNull();
    }

    @Test
    public void postShouldReturnStatus400AndListOfErrors() throws JsonProcessingException {

        String json = mapper.writeValueAsString(getBookingRequestMock());

        when(service.validateBooking(any())).thenReturn(List.of("Error1", "Error2"));

        ResponseEntity<BookingResponse<Booking>> response = restTemplate.exchange(createPostRequest(BASE_URI, json), new ParameterizedTypeReference<>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isNull();
        assertThat(response.getBody().errors).hasSize(2);
    }

    @Test
    public void postShouldReturnStatus400WhenWrongJSON() {

        final String json = "{Invalid Json}";

        ResponseEntity<String> response = restTemplate.exchange(createPostRequest(BASE_URI, json), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void getAllShouldReturnStatus200AndList(){

        when(service.findAllBookings()).thenReturn(List.of(getBookingMock(), getBookingMock()));

        ResponseEntity<BookingResponse<List<Booking>>> response = doGet(BASE_URI);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).hasSize(2);
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getAllShouldReturnStatus200AndEmptyList(){
        when(service.findAllBookings()).thenReturn(Collections.emptyList());

        ResponseEntity<BookingResponse<List<Booking>>> response = doGet(BASE_URI);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isEmpty();
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getAllBookedDatesShouldReturnStatus200AndList(){

        when(service.findAllBookedDates()).thenReturn(Set.of(LocalDate.now(), LocalDate.now().plusDays(1)));

        ResponseEntity<BookingResponse<Set<String>>> response = doGet(BASE_URI + "/booked");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).hasSize(2);
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getAllBookedShouldReturnStatus200AndEmptyList(){
        when(service.findAllBookedDates()).thenReturn(Collections.emptySet());

        ResponseEntity<BookingResponse<Set<Booking>>> response = doGet(BASE_URI + "/booked");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isEmpty();
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getAllAvailableDatesShouldReturnStatus200AndList(){

        when(service.findAllAvailableDates()).thenReturn(Set.of(LocalDate.now(), LocalDate.now().plusDays(1)));

        ResponseEntity<BookingResponse<Set<String>>> response = doGet(BASE_URI + "/available");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).hasSize(2);
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getAllAvailableShouldReturnStatus200AndEmptyList(){
        when(service.findAllAvailableDates()).thenReturn(Collections.emptySet());

        ResponseEntity<BookingResponse<Set<Booking>>> response = doGet(BASE_URI + "/available");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isEmpty();
        assertThat(response.getBody().errors).isEmpty();
    }


    @Test
    public void getByIdShouldReturnStatus200AndBooking(){
        final String uri = BASE_URI + "/abc";
        when(service.findBookingById(any())).thenReturn(Optional.of(getBookingMock()));

        ResponseEntity<BookingResponse<Booking>> response = restTemplate.exchange(
                getEndpoint(uri),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isNotNull();
        assertThat(response.getBody().errors).isEmpty();
    }

    @Test
    public void getByIdShouldReturnStatus404AndErrorMessage(){
        final String uri = BASE_URI + "/abc";
        when(service.findBookingById(any())).thenReturn(Optional.empty());

        ResponseEntity<BookingResponse<List<Booking>>> response = doGet(uri);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data).isNull();
        assertThat(response.getBody().errors).hasSize(1);
    }

    @Test
    public void deleteShouldReturnStatus204(){
        final String uri = BASE_URI + "/abc";

        RequestEntity<?> request = RequestEntity.delete(uri).build();

        ResponseEntity<?> response = restTemplate.exchange(request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private static Booking getBookingMock(){
        String randomId = String.format("RandomId-%s", Math.random());
        return Booking.of(randomId, MOCK_DATE, MOCK_DATE);
    }

    private static BookingRequest getBookingRequestMock(){
        return new BookingRequest(MOCK_DATE, MOCK_DATE);
    }

    private RequestEntity<String> createPutRequest(String uri, String jsonBody){
        return RequestEntity
                .put(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody);
    }

    private RequestEntity<String> createPostRequest(String uri, String jsonBody){
        return RequestEntity
                .post(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody);
    }

    private <T> ResponseEntity<BookingResponse<T>> doGet(final String path) {
        return restTemplate.exchange(
                getEndpoint(path),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private String getEndpoint(final String path) {
        String baseUrl = "http://localhost:%s%s";
        return String.format(baseUrl, port, path);
    }

    private static ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
