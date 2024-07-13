package com.example.demo.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {


	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;


	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		UserOrder order = null;
		try {
			User user = userRepository.findByUsername(username);
			if(user == null) {
				log.error("User not found, if you not be registered, please register before!");
				return ResponseEntity.notFound().build();
			}
			order = UserOrder.createFromCart(user.getCart());
			orderRepository.save(order);
			log.info("create order successfully: {}", order);
		} catch (RuntimeException e) {
			log.error("An unusual error has occurred, contact your service provider to find out what happened. Error occurs create order, order: {}", order);
		}
		return ResponseEntity.ok(order);
	}

	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = null;
		try {
			user = userRepository.findByUsername(username);
			if(user == null) {
				log.error("Order not found");
				return ResponseEntity.notFound().build();
			}
		} catch (RuntimeException e) {
			log.error("An unusual error has occurred, contact your service provider to find out what happened. Error occurs get order history by username, username: {}", username);
		}
		return ResponseEntity.ok(orderRepository.findByUser(user));
	}
}
