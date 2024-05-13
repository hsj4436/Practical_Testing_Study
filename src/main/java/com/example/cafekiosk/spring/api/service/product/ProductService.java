package com.example.cafekiosk.spring.api.service.product;

import com.example.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import com.example.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import com.example.cafekiosk.spring.api.service.product.response.ProductResponse;
import com.example.cafekiosk.spring.domain.product.Product;
import com.example.cafekiosk.spring.domain.product.ProductRepository;
import com.example.cafekiosk.spring.domain.product.ProductSellingStatus;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateServiceRequest request) {
        // productNumber 부여
        // DB에서 마지막 저장된 product의 productNumber 읽어와서 +1
        String nextProductNumber = createNextProductNumber();

        Product product = request.toEntity(nextProductNumber);
        Product savedProduct = productRepository.save(product);

        return ProductResponse.of(savedProduct);
    }

    public List<ProductResponse> getSellingProducts() {
        List<Product> products = productRepository.findAllBySellingStatusIn(ProductSellingStatus.forDisplay());

        return products.stream()
            .map(ProductResponse::of)
            .collect(Collectors.toList());
    }

    private String createNextProductNumber() {
        String latestProductNumber = productRepository.findLatestProductNumber();

        if (Objects.isNull(latestProductNumber)) {
            return "001";
        }

        int latestProductNumberInt = Integer.parseInt(latestProductNumber);
        int nextProductNumberInt = latestProductNumberInt + 1;

        return String.format("%03d", nextProductNumberInt);
    }
}
