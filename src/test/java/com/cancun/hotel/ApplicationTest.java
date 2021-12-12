package com.cancun.hotel;

import com.cancun.hotel.controller.BookingController;
import com.cancun.hotel.service.BookingService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTest {

	@Autowired
	BookingController controller;

	@Autowired
	BookingService service;

	@Test
	void contextLoads() {
		Assertions.assertThat(controller).isNotNull();
		Assertions.assertThat(service).isNotNull();
	}

}
