package com.github.laxika.magicalvibes.networking;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring owned by the networking module: the view-factory services that translate the
 * mutable domain model into immutable wire DTOs. Downstream modules compose this via
 * {@code @Import} rather than reaching into the networking package with their own component scan.
 */
@Configuration
@ComponentScan("com.github.laxika.magicalvibes.networking.service")
public class NetworkingConfig {
}
