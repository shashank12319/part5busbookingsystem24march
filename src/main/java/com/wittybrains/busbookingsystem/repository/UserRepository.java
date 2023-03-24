package com.wittybrains.busbookingsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.wittybrains.busbookingsystem.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	//User getUserId(int userId);
	
    Optional<User> findByUsername(String username);
}

