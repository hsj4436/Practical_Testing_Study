package com.example.cafekiosk.spring.api.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

import com.example.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import com.example.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import com.example.cafekiosk.spring.api.service.product.response.ProductResponse;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import com.example.cafekiosk.spring.domain.product.ProductSellingStatus;
import com.example.cafekiosk.spring.domain.product.ProductType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("신규 상품을 등록한다. 상품번호는 가장 최근 상품의 상품번호에서 1 증가한 값이다.")
    void createProduct() {
        // given
        Product product1 = createProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING,
            "아메리카노", 4000);
        productRepository.save(product1);

        ProductCreateServiceRequest request = ProductCreateServiceRequest.builder()
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.SELLING)
            .name("카푸치노")
            .price(5000)
            .build();

        // when
        ProductResponse productResponse = productService.createProduct(request);

        // then
        assertThat(productResponse)
            .extracting("productNumber", "type", "sellingStatus", "name", "price")
            .contains("002", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카푸치노", 5000);

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(2)
            .extracting("productNumber", "type", "sellingStatus", "name", "price")
            .containsExactlyInAnyOrder(
                tuple("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000),
                tuple("002", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카푸치노", 5000)
            );
    }

    @Test
    @DisplayName("상품이 하나도 없는 경우 신규 상품을 등록하면 상품번호는 001이다.")
    void createProductWhenProductsIsEmpty() {
        // given
        ProductCreateServiceRequest request = ProductCreateServiceRequest.builder()
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.SELLING)
            .name("카푸치노")
            .price(5000)
            .build();

        // when
        ProductResponse productResponse = productService.createProduct(request);

        // then
        assertThat(productResponse)
            .extracting("productNumber", "type", "sellingStatus", "name", "price")
            .contains("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카푸치노", 5000);

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1)
            .extracting("productNumber", "type", "sellingStatus", "name", "price")
            .contains(
                tuple("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카푸치노", 5000)
            );
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