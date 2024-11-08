package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.controller.CoinDeskController;
import com.example.demo.model.Currency;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.service.CoinDeskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class CoinDeskControllerTest {

    @MockBean
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CoinDeskController coinDeskController;

    @MockBean
    private CoinDeskService coinDeskService; 

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(coinDeskController).build();
    }

    @Test
    public void testCreateMultipleCurrencies() throws Exception {
        // 定義三個要新增的幣別
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setNameZh("美元");

        Currency gbpCurrency = new Currency();
        gbpCurrency.setCode("GBP");
        gbpCurrency.setNameZh("英鎊");

        Currency eurCurrency = new Currency();
        eurCurrency.setCode("EUR");
        eurCurrency.setNameZh("歐元");

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());
        when(currencyRepository.findByCode("GBP")).thenReturn(Optional.empty());
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());
        when(currencyRepository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 新增 USD 幣別
        mockMvc.perform(post("/api/coindesk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"code\": \"USD\", \"nameZh\": \"美元\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("USD"))
                .andExpect(jsonPath("$.nameZh").value("美元"));

        // 新增 GBP 幣別
        mockMvc.perform(post("/api/coindesk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"code\": \"GBP\", \"nameZh\": \"英鎊\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("GBP"))
                .andExpect(jsonPath("$.nameZh").value("英鎊"));

        // 新增 EUR 幣別
        mockMvc.perform(post("/api/coindesk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"code\": \"EUR\", \"nameZh\": \"歐元\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.nameZh").value("歐元"));

        // 確認調用次數為 3
        verify(currencyRepository, times(3)).save(any(Currency.class));
    }

    @Test
    public void testUpdateCurrency() throws Exception {
        String code = "USD";
        Currency existingCurrency = new Currency();
        existingCurrency.setId(1L);
        existingCurrency.setCode("USD");
        existingCurrency.setNameZh("美元");
        
        Currency updatedCurrency = new Currency();
        updatedCurrency.setId(1L);
        updatedCurrency.setCode("USD");
        updatedCurrency.setNameZh("美國美元");

        when(currencyRepository.findByCode(code)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.save(any(Currency.class))).thenReturn(updatedCurrency);


        String requestJson = "{ \"code\": \"USD\", \"nameZh\": \"美國美元\"}";
        // 執行 PUT 請求並檢查結果
        mockMvc.perform(put("/api/coindesk/" + code)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedCurrency.getId()))
                .andExpect(jsonPath("$.code").value(updatedCurrency.getCode()))
                .andExpect(jsonPath("$.nameZh").value("美國美元"));

        verify(currencyRepository).save(any(Currency.class));
    }
    
    @Test
    public void testDeleteCurrency() throws Exception {
        String code = "USD";
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setNameZh("美元");
        when(currencyRepository.findByCode(code)).thenReturn(Optional.of(currency));

        mockMvc.perform(delete("/api/coindesk/{code}", code))
                .andExpect(status().isNoContent());  // 期望返回 204 No Content

        verify(currencyRepository, times(1)).deleteByCode(code);
    }

    @Test
    public void testGetCurrentprice_ShouldReturnCoindeskData() throws Exception {
        String jsonResponse = "{\n" +
                "\"time\": {\n" +
                "\"updated\": \"Nov 7, 2024 07:54:35 UTC\",\n" +
                "\"updatedISO\": \"2024-11-07T07:54:35+00:00\",\n" +
                "\"updateduk\": \"Nov 7, 2024 at 07:54 GMT\"\n" +
                "},\n" +
                "\"disclaimer\": \"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org\",\n" +
                "\"chartName\": \"Bitcoin\",\n" +
                "\"bpi\": {\n" +
                "\"USD\": {\n" +
                "\"code\": \"USD\",\n" +
                "\"symbol\": \"&#36;\",\n" +
                "\"rate\": \"74,788.398\",\n" +
                "\"description\": \"United States Dollar\",\n" +
                "\"rate_float\": 74788.3983\n" +
                "},\n" +
                "\"GBP\": {\n" +
                "\"code\": \"GBP\",\n" +
                "\"symbol\": \"&pound;\",\n" +
                "\"rate\": \"57,834.093\",\n" +
                "\"description\": \"British Pound Sterling\",\n" +
                "\"rate_float\": 57834.0928\n" +
                "},\n" +
                "\"EUR\": {\n" +
                "\"code\": \"EUR\",\n" +
                "\"symbol\": \"&euro;\",\n" +
                "\"rate\": \"69,575.123\",\n" +
                "\"description\": \"Euro\",\n" +
                "\"rate_float\": 69575.1234\n" +
                "}\n" +
                "}\n" +
                "}";
            JsonNode mockResponse = objectMapper.readTree(jsonResponse);

            when(coinDeskService.getCoindeskData()).thenReturn(mockResponse);
        
            mockMvc.perform(get("/api/coindesk/currentDate"))
                    .andExpect(status().isOk())  // 期望 200 OK
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));
            
            verify(coinDeskService, times(1)).getCoindeskData();
    }  

    @Test
    public void testGetCoinDeskData_ShouldReturnTransformedData() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = "{ \"update_time\": \"2024/11/07 19:01:05\", \"currencies\": [ { \"name_zh\": \"美元\", \"code\": \"USD\", \"rate\": \"76,504.508\" }, { \"name_zh\": \"英鎊\", \"code\": \"GBP\", \"rate\": \"58,893.706\" }, { \"name_zh\": \"歐元\", \"code\": \"EUR\", \"rate\": \"70,805.534\" } ] }";
        JsonNode coindeskDataNode = objectMapper.readTree(jsonData);

        when(coinDeskService.getCoindeskData()).thenReturn(coindeskDataNode);

        // 準備 transformedData 返回值
        Map<String, Object> transformedData = new HashMap<>();
        transformedData.put("update_time", "2024/11/07 19:01:05");

        Map<String, Object> currencyUSD = new HashMap<>();
        currencyUSD.put("name_zh", "美元");
        currencyUSD.put("code", "USD");
        currencyUSD.put("rate", "76,504.508");

        Map<String, Object> currencyGBP = new HashMap<>();
        currencyGBP.put("name_zh", "英鎊");
        currencyGBP.put("code", "GBP");
        currencyGBP.put("rate", "58,893.706");

        Map<String, Object> currencyEUR = new HashMap<>();
        currencyEUR.put("name_zh", "歐元");
        currencyEUR.put("code", "EUR");
        currencyEUR.put("rate", "70,805.534");

        transformedData.put("currencies", List.of(currencyUSD, currencyGBP, currencyEUR));
        when(coinDeskService.transformData(coindeskDataNode)).thenReturn(transformedData);

        // 執行 GET 請求＆驗證
        MvcResult result = mockMvc.perform(get("/api/coindesk/transformData"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currencies[0].name_zh").value("美元"))
                .andExpect(jsonPath("$.currencies[1].name_zh").value("英鎊"))
                .andExpect(jsonPath("$.currencies[2].name_zh").value("歐元"))
                .andReturn();
        String responseJson = result.getResponse().getContentAsString();
        System.out.println("Response JSON: " + responseJson);
    }
}