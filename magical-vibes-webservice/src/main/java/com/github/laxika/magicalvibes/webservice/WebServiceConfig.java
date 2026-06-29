package com.github.laxika.magicalvibes.webservice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring owned by the web-service module: the lobby/login/draft/deck services, the
 * {@code MessageHandler} implementation ({@code GameMessageHandler}), and the SPA forwarding
 * controller. Scans the module's own packages so the application can compose it via
 * {@code @Import} rather than reaching in with its own component scan.
 *
 * <p>JPA entities and repositories live in this module too, but are picked up by the
 * application's {@code @EntityScan} / {@code @EnableJpaRepositories} rather than component scan.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.github.laxika.magicalvibes.webservice",
        "com.github.laxika.magicalvibes.handler"
})
public class WebServiceConfig {
}
