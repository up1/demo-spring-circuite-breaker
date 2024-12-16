package com.example.orders.order;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    private static final String GET_SERVICE_NAME = "order-service-get";
    private static final String INVENTORY_SERVICE_URL = "http://localhost:8081/api/item/";

    @CircuitBreaker(name = GET_SERVICE_NAME, fallbackMethod = "getDefaultOrders")
    public Order getOrderById(Integer id) {
        // Call external service via HTTP protocol
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Item> entity = new HttpEntity<>(null, headers);
        ResponseEntity<Item> response = restTemplate.exchange(
                (INVENTORY_SERVICE_URL + id),
                HttpMethod.GET, entity,
                Item.class);
        Item item = response.getBody();

        // Get order detail from database
        Order order = null;
        if (item != null) {
            System.out.println("Item exists");
            order = orderRepository.findById(id).orElse(null);
        }
        return order;
    }

    public Order getDefaultOrders(Exception e) throws Exception {
        throw new RuntimeException("No item found for the provided order");
    }

}
