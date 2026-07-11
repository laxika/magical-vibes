package com.github.laxika.magicalvibes.carddata;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring owned by the card-data module: oracle-data loading and caching. Downstream modules
 * compose this via {@code @Import} rather than reaching into the carddata packages with their own
 * component scan.
 */
@Configuration
@ComponentScan("com.github.laxika.magicalvibes.carddata")
public class CardDataConfiguration {
}
