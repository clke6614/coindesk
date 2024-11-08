package com.example.demo.service;

import com.example.demo.repository.CurrencyRepository;
import com.example.demo.model.Currency;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CoinDeskService {

    private final WebClient webClient;
    private final String COINDESK_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    public CoinDeskService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(COINDESK_URL).build();
    }

    // Call CoinDesk API 獲取資料
    public JsonNode getCoindeskData() {
        return this.webClient.get()
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    // 轉換指定格式
    public Map<String, Object> transformData(JsonNode data) {
        Map<String, Object> transformedData = new HashMap<>();
        String updatedISO = data.path("time").path("updatedISO").asText();
        String formattedTime = formatTime(updatedISO);
        transformedData.put("update_time", formattedTime);

        // 幣別資訊
        List<Map<String, String>> currencies = new ArrayList<>();
        data.path("bpi").fields().forEachRemaining(entry -> {
            String code = entry.getKey();
            JsonNode currencyData = entry.getValue();
            Map<String, String> currencyInfo = new HashMap<>();
            currencyInfo.put("code", code);
            currencyInfo.put("rate", currencyData.path("rate").asText());
            currencyInfo.put("name_zh", getChineseName(code));
            currencies.add(currencyInfo);
        });

        transformedData.put("currencies", currencies);
        return transformedData;
    }

    private String formatTime(String isoTime) {
        ZonedDateTime dateTime = ZonedDateTime.parse(isoTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    private String getChineseName(String code) {
        return currencyRepository.findByCode(code)
                .map(Currency::getNameZh)
                .orElse("N/A");
    }
}