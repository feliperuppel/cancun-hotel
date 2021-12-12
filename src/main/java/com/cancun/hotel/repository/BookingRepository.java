package com.cancun.hotel.repository;

import com.cancun.hotel.domain.Booking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends CrudRepository<Booking, String> {
    List<Booking> findAll();
}
