package com.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class SpringaiApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainStartsApplication() {
        // No web server, so it doesn't clash with an app already running on port 8080.
        assertDoesNotThrow(() -> SpringaiApplication.main(new String[] {"--spring.main.web-application-type=none"}));
    }
}
