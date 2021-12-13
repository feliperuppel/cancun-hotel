package com.cancun.hotel.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class BookingResponse<T> {

    public final T data;
    public final List<String> errors;

    private BookingResponse(T data, List<String> errors){
        this.data = data;
        this.errors = errors;
    }

    public static <T> BookingResponse<T> of(@JsonProperty("data") T data){
        return new BookingResponse<>(data, Collections.emptyList());
    }

    public static <T> BookingResponse<T> of(@JsonProperty("errors") List<String> errors){
        return new BookingResponse<>(null, errors);
    }
}
