
package com.wittybrains.busbookingsystem.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wittybrains.busbookingsystem.dto.BookingDTO;
import com.wittybrains.busbookingsystem.dto.TravelScheduleDTO;
import com.wittybrains.busbookingsystem.dto.UserDTO;
import com.wittybrains.busbookingsystem.exception.InsufficientSeatException;
import com.wittybrains.busbookingsystem.exception.TravelScheduleNotFoundException;
import com.wittybrains.busbookingsystem.exception.UserNotFoundException;
import com.wittybrains.busbookingsystem.model.Booking;
import com.wittybrains.busbookingsystem.model.TravelSchedule;
import com.wittybrains.busbookingsystem.model.User;
import com.wittybrains.busbookingsystem.repository.BookingRepository;
import com.wittybrains.busbookingsystem.repository.BusRepository;
import com.wittybrains.busbookingsystem.repository.TravelScheduleRepository;
import com.wittybrains.busbookingsystem.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookingService {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final TravelScheduleRepository travelScheduleRepository;
	private final Logger logger = LoggerFactory.getLogger(BookingService.class);

	public BookingService(BookingRepository bookingRepository, BusRepository busRepository,
			TravelScheduleRepository travelScheduleRepository, UserRepository userRepository) {
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
		this.travelScheduleRepository = travelScheduleRepository;

	}

	
	
	public BookingDTO createBooking(BookingDTO bookingDTO) {
	    logger.info("Creating booking with bookingDTO: {}", bookingDTO);

	    if (bookingDTO.getSchedule() == null || bookingDTO.getSchedule().getId() == null) {
	        throw new IllegalArgumentException("Schedule ID cannot be null");
	    }
	    if (bookingDTO.getUser() == null || bookingDTO.getUser().getId() == null) {
	        throw new IllegalArgumentException("User ID cannot be null");
	    }

	    // Retrieve user and travel schedule
	    UserDTO userDTO = bookingDTO.getUser();
	    Optional<User> optionalUser = userRepository.findById(userDTO.getId());
	    User user = optionalUser.orElseThrow(() -> new UserNotFoundException("User not found for id: " + userDTO.getId()));
	    Optional<TravelSchedule> optionalTravelSchedule = travelScheduleRepository.findById(bookingDTO.getSchedule().getId());
	    TravelSchedule travelSchedule = optionalTravelSchedule.orElseThrow(() -> new TravelScheduleNotFoundException("Travel schedule not found for id: " + bookingDTO.getSchedule().getId()));

	    // Calculate costs
	    int numberOfSeats = bookingDTO.getNumberOfSeats();
	    double seatCost = travelSchedule.getSeatCost();
	    double subtotal = numberOfSeats * seatCost;
	    double gst = subtotal * 0.12;
	    double totalAmount = subtotal + gst;

	    // Check available seats
	    int totalSeats = travelSchedule.getTotalSeat();
	    int seatsBooked = travelSchedule.getSeatBooked();
	    int availableSeats = totalSeats - seatsBooked;
	    if (numberOfSeats > availableSeats) {
	        logger.warn("Cannot book {} seats, only {} seats are available", numberOfSeats, availableSeats);
	        throw new InsufficientSeatException("Insufficient seats available");
	    }

	    // Update travel schedule and save booking
	    travelSchedule.setSeatBooked(seatsBooked + numberOfSeats);
	    travelSchedule.setAvailableSeat(availableSeats - numberOfSeats);
	    travelScheduleRepository.save(travelSchedule);
	    Booking booking = new Booking();
	    booking.setUser(user);
	    booking.setSchedule(travelSchedule);
	    booking.setSeatCost(seatCost);
	    booking.setNumberOfSeats(numberOfSeats);
	    booking.setTotalAmount(totalAmount);
	    //booking.setTotalSeat(totalSeats);
	    booking.setSeatBooked(seatsBooked + numberOfSeats);
	    booking.setAvailableSeat(availableSeats - numberOfSeats);
	    Booking savedBooking = bookingRepository.save(booking);
	    logger.info("Booking created with savedBooking: {}", savedBooking);
	    return new BookingDTO(savedBooking);
	}
}

