package com.github.laxika.magicalvibes.scryfall;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring owned by the scryfall module: oracle-data loading and caching. Downstream modules
 * compose this via {@code @Import} rather than reaching into the scryfall package with their own
 * component scan.
 */
@Configuration
@ComponentScan("com.github.laxika.magicalvibes.scryfall")
public class ScryfallConfig {
}
