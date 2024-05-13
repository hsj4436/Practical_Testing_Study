package com.example.cafekiosk.spring.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("원하는 판매상태를 가진 상품들을 조회한다.")
    void findAllBySellingStatusIn() {
        // given
        Product product = Product.builder()
            .productNumber("001")
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.SELLING)
            .name("아메리카노")
            .price(4000)
            .build();

        Product product2 = Product.builder()
            .productNumber("002")
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.HOLD)
            .name("카페라떼")
            .price(4500)
            .build();

        Product product3 = Product.builder()
            .productNumber("003")
            .type(ProductType.HANDMADE)
            .sellingStatus(ProductSellingStatus.STOP_SELLING)
            .name("팥빙수")
            .price(7000)
            .build();

        productRepository.saveAll(List.of(product, product2, product3));

        // when
        List<Product> products = productRepository.findAllBySellingStatusIn(
            List.of(ProductSellingStatus.SELLING, ProductSellingStatus.HOLD));

        // then
        assertThat(products).hasSize(2)
            .extracting("productNumber", "name", "sellingStatus")
            .containsExactlyInAnyOrder(
                tuple("001", "아메리카노", ProductSellingStatus.SELLING),
                tuple("002", "카페라떼", ProductSellingStatus.HOLD)
            );
    }

    @Test
    @DisplayName("상품번호 리스트로 상품들을 조회한다.")
    void findAllByProductNumberIn() {
        // given
        Product product = createProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING,
            "아메리카노", 4000);

        Product product2 = createProduct("002", ProductType.HANDMADE, ProductSellingStatus.HOLD,
            "카페라떼", 4500);

        Product product3 = createProduct("003", ProductType.HANDMADE,
            ProductSellingStatus.STOP_SELLING, "팥빙수", 7000);

        productRepository.saveAll(List.of(product, product2, product3));

        // when
        List<Product> products = productRepository.findAllByProductNumberIn(List.of("001", "002"));

        // then
        assertThat(products).hasSize(2)
            .extracting("productNumber", "name", "sellingStatus")
            .containsExactlyInAnyOrder(
                tuple("001", "아메리카노", ProductSellingStatus.SELLING),
                tuple("002", "카페라떼", ProductSellingStatus.HOLD)
            );
    }

    @Test
    @DisplayName("가장 마지막으로 저장한 상품의 상품번호를 읽어온다.")
    void findLatestProductNumber() {
        // given
        String targetProductNumber = "003";

        Product product = createProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING,
            "아메리카노", 4000);

        Product product2 = createProduct("002", ProductType.HANDMADE, ProductSellingStatus.HOLD,
            "카페라떼", 4500);

        Product product3 = createProduct(targetProductNumber, ProductType.HANDMADE,
            ProductSellingStatus.STOP_SELLING, "팥빙수", 7000);

        productRepository.saveAll(List.of(product, product2, product3));

        // when
        String lastProductNumber = productRepository.findLatestProductNumber();

        // then
        assertThat(lastProductNumber).isEqualTo(targetProductNumber);
    }

    @Test
    @DisplayName("가장 마지막으로 저장한 상품의 상품번호를 읽어올 때, 등록된 상품이 하나도 없는 경우 null을 반환한다.")
    void findLatestProductNumberWhenProductIsEmpty() {
        // given

        // when
        String lastProductNumber = productRepository.findLatestProductNumber();

        // then
        assertThat(lastProductNumber).isNull();
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