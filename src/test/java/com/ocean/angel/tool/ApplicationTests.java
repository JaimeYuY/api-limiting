package com.ocean.angel.tool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@SpringBootTest
class ApplicationTests {

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }, "/*")
                .build();
    }

    @Test
    void contextLoads() {
        try {
            for(int i = 0; i < 50; i++) {
                MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/test/limited/resource")).andReturn();
                if(HttpStatus.OK.value() == mvcResult.getResponse().getStatus()) {
                    log.info("{}", mvcResult.getResponse().getContentAsString());
                }
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }
}
