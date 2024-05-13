package com.example.cafekiosk.spring.api.controller.order;

import com.example.cafekiosk.spring.api.ApiResponse;
import com.example.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import com.example.cafekiosk.spring.api.service.order.OrderService;
import com.example.cafekiosk.spring.api.service.order.response.OrderResponse;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders/new")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        LocalDateTime registeredDateTime = LocalDateTime.now();
        return ApiResponse.ok(orderService.createOrder(orderCreateRequest.toServiceRequest(), registeredDateTime));
    }

}
