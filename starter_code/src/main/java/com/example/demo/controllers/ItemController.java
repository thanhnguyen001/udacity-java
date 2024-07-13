package com.example.demo.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RestController
@RequestMapping("/api/item")
@Slf4j
public class ItemController {

	@Autowired
	private ItemRepository itemRepository;

	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		return ResponseEntity.ok(itemRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		if (!itemRepository.findById(id).isPresent()) {
			log.error("Item not found, item id: {}", id);
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.of(itemRepository.findById(id));
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		List<Item> items = itemRepository.findByName(name);
		if (items == null || items.isEmpty()) {
			log.error("Item not found, item name {}", name);
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(items);

	}

	@PostMapping("/create")
	public ResponseEntity<Item> createItem(@RequestBody Item item) {
		if (item != null && item.getName() != null) {
			itemRepository.save(item);
			log.info("create Item successfully: item = {}", item);
			return ResponseEntity.ok(item);
		}
		log.error("An unusual error has occurred, contact your service provider to find out what happened. Error occurs create item, item: {}", item);
		return ResponseEntity.badRequest().body(null);
	}
}