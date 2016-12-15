package org.bonitasoft.zen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Laurent Leseigneur
 */
@SpringBootApplication
@ComponentScan
public class ZenApp extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZenApp.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZenApp.class);
    }
}
