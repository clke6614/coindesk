package com.example.demo.controller;

import com.example.demo.model.Currency;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.service.CoinDeskService;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/coindesk")
public class CoinDeskController {

    @Autowired
    private CoinDeskService coinDeskService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @GetMapping("/transformData")
    public ResponseEntity<Map<String, Object>> getCoinDeskData() {
        JsonNode coindeskData = coinDeskService.getCoindeskData(); 
        Map<String, Object> transformedData = coinDeskService.transformData(coindeskData);
        return new ResponseEntity<>(transformedData, HttpStatus.OK);
    }

    @GetMapping("/currentDate")
    public ResponseEntity<JsonNode> getCurrentprice() {
        JsonNode coindeskData = coinDeskService.getCoindeskData();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(coindeskData);
    }

    // 查詢幣別
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrency(@PathVariable String code) {
        Optional<Currency> currency = currencyRepository.findByCode(code);
        return currency.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 查詢所有幣別
    @GetMapping
    public ResponseEntity<Iterable<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyRepository.findAll());
    }

    // 新增幣別
    @PostMapping
    public ResponseEntity<Currency> createCurrency(@RequestBody Currency currency) {
        currency.setCreateTime(LocalDateTime.now());
        if (currencyRepository.findByCode(currency.getCode()).isPresent()) {
            return ResponseEntity.status(400).build();
        }
        Currency savedCurrency = currencyRepository.save(currency);
        return ResponseEntity.status(201).body(savedCurrency);
    }

    // 修改幣別
    @PutMapping("/{code}")
    public ResponseEntity<Currency> updateCurrency(@PathVariable String code, @RequestBody Currency currency) {
        return currencyRepository.findByCode(code)
            .map(existingCurrency -> {
                currency.setId(existingCurrency.getId());
                currency.setCode(code);
                Currency updatedCurrency = currencyRepository.save(currency); 
                return ResponseEntity.ok(updatedCurrency); 
            })
            .orElseGet(() -> ResponseEntity.notFound().build());  // 若找不到返回404
    }

    // 刪除幣別
    @DeleteMapping("/{code}")
    @Transactional
    public ResponseEntity<Void> deleteCurrency(@PathVariable String code) {
        Optional<Currency> currencyOptional = currencyRepository.findByCode(code);
        // 如果找不到該幣別，返回 404 Not Found
        if (!currencyOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        // 找到該幣別，進行刪除
        currencyRepository.deleteByCode(code);
        // 刪除成功返回 204 No Content
        return ResponseEntity.noContent().build();
    }
}
