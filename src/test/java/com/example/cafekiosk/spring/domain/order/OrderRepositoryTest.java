package com.example.cafekiosk.spring.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.example.cafekiosk.spring.IntegrationTestSupport;
import com.example.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import com.example.cafekiosk.spring.domain.product.ProductSellingStatus;
import com.example.cafekiosk.spring.domain.product.ProductType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OrderRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("특정 날짜의 결제 완료된 주문을 조회할 수 있다.")
    void test() {
        // given
        LocalDate date = LocalDate.of(2024, 5, 10);

        Product product1 = createProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING,
            "아메리카노", 4000);
        Product product2 = createProduct("002", ProductType.HANDMADE, ProductSellingStatus.HOLD,
            "카페라떼", 4500);
        Product product3 = createProduct("003", ProductType.HANDMADE,
            ProductSellingStatus.STOP_SELLING, "팥빙수", 7000);
        productRepository.saveAll(List.of(product1, product2, product3));

        Order order1 = Order.builder()
            .products(List.of(product1, product2))
            .orderStatus(OrderStatus.PAYMENT_COMPLETE)
            .registeredDateTime(LocalDateTime.of(date, LocalTime.of(12, 10)))
            .build();
        Order order2 = Order.builder()
            .products(List.of(product1))
            .orderStatus(OrderStatus.CANCELED)
            .registeredDateTime(LocalDateTime.of(date, LocalTime.of(12, 15)))
            .build();
        Order order3 = Order.builder()
            .products(List.of(product2, product3))
            .orderStatus(OrderStatus.PAYMENT_FAILED)
            .registeredDateTime(LocalDateTime.of(date, LocalTime.of(16, 34)))
            .build();

        orderRepository.saveAll(List.of(order1, order2, order3));

        // when
        List<Order> orders = orderRepository.findOrdersBy(date.atStartOfDay(),
            date.plusDays(1).atStartOfDay(), OrderStatus.PAYMENT_COMPLETE);

        // then
        assertThat(orders).hasSize(1)
            .extracting("totalPrice", "orderStatus")
            .contains(
                tuple(8500, OrderStatus.PAYMENT_COMPLETE)
            );
        assertThat(orders.get(0).getOrderProducts()).hasSize(2);
    }

    private Product createProduct(String productNumber, ProductType type,
        ProductSellingStatus sellingStatus, String name, int price) {
        return Product.builder()
            .productNumber(productNumber)
            .type(type)
            .sellingStatus(sellingStatus)
            .name(name)
            .price(price)
            .build();
    }
}