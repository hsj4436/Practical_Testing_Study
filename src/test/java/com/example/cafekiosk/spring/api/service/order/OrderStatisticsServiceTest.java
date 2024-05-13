package com.example.cafekiosk.spring.api.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.cafekiosk.spring.client.mail.MailSendClient;
import com.example.cafekiosk.spring.domain.history.mail.MailSendHistory;
import com.example.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;
import com.example.cafekiosk.spring.domain.order.Order;
import com.example.cafekiosk.spring.domain.order.OrderRepository;
import com.example.cafekiosk.spring.domain.order.OrderStatus;
import com.example.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import com.example.cafekiosk.spring.domain.product.ProductSellingStatus;
import com.example.cafekiosk.spring.domain.product.ProductType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class OrderStatisticsServiceTest {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MailSendHistoryRepository mailSendHistoryRepository;

    @MockBean
    private MailSendClient mailSendClient;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        mailSendHistoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("결제 완료 주문들을 조회하여 매출 통계 메일을 전송한다.")
    void sendOrderStatisticsMail() {
        // given
        LocalDateTime now = LocalDateTime.of(2024, 5, 10, 0, 0);

        Product product1 = createProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING,
            "아메리카노", 4000);
        Product product2 = createProduct("002", ProductType.HANDMADE, ProductSellingStatus.HOLD,
            "카페라떼", 4500);
        Product product3 = createProduct("003", ProductType.HANDMADE,
            ProductSellingStatus.STOP_SELLING, "팥빙수", 7000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        Order order1 = createPaymentCompletedOrder(LocalDateTime.of(2024, 5, 9, 23, 59), products);
        Order order2 = createPaymentCompletedOrder(now, products);
        Order order3 = createPaymentCompletedOrder(LocalDateTime.of(2024, 5, 10, 23, 59), products);
        Order order4 = createPaymentCompletedOrder(LocalDateTime.of(2024, 5, 11, 0, 0), products);

        // stubbing
        when(mailSendClient.sendEmail(any(String.class), any(String.class), any(String.class),
            any(String.class)))
            .thenReturn(true);

        // when
        boolean result = orderStatisticsService.sendOrderStatisticsMail(LocalDate.of(2024, 5, 10),
            "test@test.com");

        // then
        assertThat(result).isTrue();

        List<MailSendHistory> histories = mailSendHistoryRepository.findAll();
        assertThat(histories).hasSize(1)
            .extracting("content")
            .contains("총 매출 합계는 31000원입니다.");
    }

    private Order createPaymentCompletedOrder(LocalDateTime now, List<Product> products) {
        Order order = Order.builder()
            .products(products)
            .orderStatus(OrderStatus.PAYMENT_COMPLETE)
            .registeredDateTime(now)
            .build();

        return orderRepository.save(order);
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