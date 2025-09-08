package com.example.checkout_service.service;

import com.example.checkout_service.dto.BundleAppliedDTO;
import com.example.checkout_service.dto.ReceiptDTO;
import com.example.checkout_service.dto.ReceiptItemDTO;
import com.example.checkout_service.model.BundleDeal;
import com.example.checkout_service.model.CartItem;
import com.example.checkout_service.model.CheckoutSession;
import com.example.checkout_service.repository.BundleDealRepository;
import com.example.checkout_service.repository.CheckoutSessionRepository;
import com.example.checkout_service.repository.MultiPriceRuleRepository;
import com.example.checkout_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CheckoutService {

    private final ProductRepository productRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final MultiPriceRuleRepository multiPriceRuleRepository;
    private final BundleDealRepository bundleDealRepository;

    public CheckoutService(ProductRepository productRepository, CheckoutSessionRepository checkoutSessionRepository,
                           MultiPriceRuleRepository multiPriceRuleRepository, BundleDealRepository bundleDealRepository) {
        this.productRepository = productRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.multiPriceRuleRepository = multiPriceRuleRepository;
        this.bundleDealRepository = bundleDealRepository;
    }

    public String startSession() {
        String sessionId = UUID.randomUUID().toString();
        CheckoutSession session = new CheckoutSession();
        session.setSessionId(sessionId);
        checkoutSessionRepository.save(session);
        return sessionId;
    }

    public void scan(String sessionId, String sku) {
        var product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SKU: " + sku));

        var session = checkoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        var existingItem = session.getItems().stream()
                .filter(i -> i.getProduct().getSku().equals(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(1);
            session.getItems().add(newItem);
        }

        checkoutSessionRepository.save(session);
    }

        public List<CartItem> getCart(String sessionId) {
        var session = checkoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        return session.getItems();
    }

    public ReceiptDTO checkout(String sessionId) {
        var session = checkoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        List<ReceiptItemDTO> receiptItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<BundleAppliedDTO> appliedBundles = new ArrayList<>();


        for (var item : session.getItems()) {
            BigDecimal itemTotal;
            BigDecimal discountedUnitPrice;

            var ruleOpt = multiPriceRuleRepository.findBySku(item.getProduct().getSku());

            if (ruleOpt.isPresent() && item.getQuantity() >= ruleOpt.get().getRequiredQuantity()) {
                var rule = ruleOpt.get();

                discountedUnitPrice = rule.getSpecialPrice();

                itemTotal = discountedUnitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            } else {
                discountedUnitPrice = item.getProduct().getUnitPrice();
                itemTotal = discountedUnitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            }

            receiptItems.add(new ReceiptItemDTO(
                    item.getProduct().getName(),
                    item.getProduct().getUnitPrice(),
                    discountedUnitPrice,
                    item.getQuantity(),
                    itemTotal
            ));

            total = total.add(itemTotal);
        }

        List<BundleDeal> deals = bundleDealRepository.findAll();
        for (BundleDeal deal : deals) {
            Optional<CartItem> item1Opt = session.getItems().stream()
                    .filter(i -> i.getProduct().getSku().equals(deal.getSku1()))
                    .findFirst();
            Optional<CartItem> item2Opt = session.getItems().stream()
                    .filter(i -> i.getProduct().getSku().equals(deal.getSku2()))
                    .findFirst();

            if (item1Opt.isPresent() && item2Opt.isPresent()) {
                int applicableSets = Math.min(item1Opt.get().getQuantity(), item2Opt.get().getQuantity());
                BigDecimal discount = deal.getDiscount().multiply(BigDecimal.valueOf(applicableSets));

                appliedBundles.add(new BundleAppliedDTO(
                        deal.getSku1(),
                        deal.getSku2(),
                        applicableSets,
                        discount
                ));

                total = total.subtract(discount);
            }
        }

        return new ReceiptDTO(receiptItems, appliedBundles, total);
    }
}
