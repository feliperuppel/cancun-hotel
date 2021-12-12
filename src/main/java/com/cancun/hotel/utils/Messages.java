package com.cancun.hotel.utils;

public abstract class Messages {
    public static final String CHECK_IN_NOT_NULL = "CheckIn must be not null";
    public static final String CHECK_OUT_NOT_NULL = "CheckOut must be not null";
    public static final String CHECK_IN_AFTER_CHECK_OUT = "CheckIn cannot at the same date or after CheckOut";
    public static final String STAY_TOO_LONG = "Stay cannot be longer than %s days";
    public static final String TOO_EARLY = "%s must be after or at %s";
    public static final String TOO_LATE = "%s must be before or at %s";
    public static final String ALREADY_BOOKED = "One or more days of your desired period are already booked";
    public static final String NO_BOOKING_FOUND_FOR_GIVEN_ID = "No booking found with id : %s";
}
