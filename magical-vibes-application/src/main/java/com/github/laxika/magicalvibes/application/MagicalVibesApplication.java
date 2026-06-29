package com.github.laxika.magicalvibes.application;

import com.github.laxika.magicalvibes.ai.AiConfig;
import com.github.laxika.magicalvibes.service.GameEngineConfig;
import com.github.laxika.magicalvibes.webservice.WebServiceConfig;
import com.github.laxika.magicalvibes.websocket.configuration.WebSocketConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Application composition root. Lives in its own {@code application} package so the default
 * component scan finds nothing but this class — every module contributes its beans through its
 * own {@code @Configuration} composed here via {@code @Import}, instead of the app reaching into
 * sibling packages with a broad component scan.
 *
 * <p>JPA entities and repositories live in the web-service module, so they are wired explicitly
 * via {@link EntityScan} / {@link EnableJpaRepositories} rather than relying on the
 * auto-configuration package (which now resolves to this {@code application} package).
 */
@SpringBootApplication
@EntityScan("com.github.laxika.magicalvibes.entity")
@EnableJpaRepositories("com.github.laxika.magicalvibes.repository")
@Import({GameEngineConfig.class, WebSocketConfiguration.class, AiConfig.class, WebServiceConfig.class})
public class MagicalVibesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicalVibesApplication.class, args);
    }
}
