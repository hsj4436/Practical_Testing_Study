package com.example.cafekiosk.spring.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductSellingStatus;
import com.example.cafekiosk.spring.domain.product.ProductType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 상품 리스트에서 주문의 총 금액을 계산한다.")
    void calculateTotalPrice() {
        // given
        List<Product> products = List.of(
            createProduct("001", 4000),
            createProduct("002", 4500)
        );

        // when
        Order order = Order.create(products, LocalDateTime.now());

        // then
        assertThat(order.getTotalPrice()).isEqualTo(8500);
    }

    @Test
    @DisplayName("주문 생성 시 주문 상태는 INIT이다.")
    void init() {
        // given
        List<Product> products = List.of(
            createProduct("001", 4000),
            createProduct("002", 4500)
        );

        // when
        Order order = Order.create(products, LocalDateTime.now());

        // then
        assertThat(order.getOrderStatus()).isEqualByComparingTo(OrderStatus.INIT);
    }

    @Test
    @DisplayName("주문 생성 시 등록 시간을 기록한다.")
    void registeredDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        List<Product> products = List.of(
            createProduct("001", 4000),
            createProduct("002", 4500)
        );

        // when
        Order order = Order.create(products, registeredDateTime);

        // then
        assertThat(order.getRegisteredDateTime()).isEqualTo(registeredDateTime);
    }

    private Product createProduct(String productNumber, int price) {
        return Product.builder()
            .productNumber(productNumber)
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.SELLING)
            .name("메뉴 이름")
            .price(price)
            .build();
    }
}