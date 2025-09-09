package com.example.checkout_service.config;

import com.example.checkout_service.model.BundleDeal;
import com.example.checkout_service.model.MultiPriceRule;
import com.example.checkout_service.model.Product;
import com.example.checkout_service.repository.BundleDealRepository;
import com.example.checkout_service.repository.MultiPriceRuleRepository;
import com.example.checkout_service.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final MultiPriceRuleRepository multiPriceRuleRepository;
    private final BundleDealRepository bundleDealRepository;

    public DataInitializer(ProductRepository productRepository,
                           MultiPriceRuleRepository multiPriceRuleRepository,
                           BundleDealRepository bundleDealRepository) {
        this.productRepository = productRepository;
        this.multiPriceRuleRepository = multiPriceRuleRepository;
        this.bundleDealRepository = bundleDealRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "A", "Product A", BigDecimal.valueOf(40)));
            productRepository.save(new Product(null, "B", "Product B", BigDecimal.valueOf(10)));
            productRepository.save(new Product(null, "C", "Product C", BigDecimal.valueOf(30)));
            productRepository.save(new Product(null, "D", "Product D", BigDecimal.valueOf(25)));
        }

        if (multiPriceRuleRepository.count() == 0) {
            multiPriceRuleRepository.save(new MultiPriceRule(null, "A", 3, BigDecimal.valueOf(30)));
            multiPriceRuleRepository.save(new MultiPriceRule(null, "B", 2, BigDecimal.valueOf(7.5)));
            multiPriceRuleRepository.save(new MultiPriceRule(null, "C", 4, BigDecimal.valueOf(20)));
            multiPriceRuleRepository.save(new MultiPriceRule(null, "D", 2, BigDecimal.valueOf(23.5)));
        }

        if (bundleDealRepository.count() == 0) {
            bundleDealRepository.save(new BundleDeal(null, "A", "B", BigDecimal.valueOf(2.5))); // przykładowy bundle
            bundleDealRepository.save(new BundleDeal(null, "C", "D", BigDecimal.valueOf(5)));   // C+D -> 5 off
        }

        System.out.println("DataInitializer: initial data loaded if missing");
    }
}
