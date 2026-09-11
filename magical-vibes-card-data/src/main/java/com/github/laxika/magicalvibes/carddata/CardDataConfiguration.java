package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    /**
     * Declared here rather than annotated because {@link RandomDeckGenerator} lives in
     * {@code magical-vibes-card}, which has no Spring on its classpath — that module holds the card
     * definitions and stays framework-free.
     */
    @Bean
    RandomDeckGenerator randomDeckGenerator(
            CardRegistry cardRegistry,
            @Value("${oracle.data-load-mode:EAGER}") OracleLoadMode loadMode) {
        RandomDeckGenerator generator = new RandomDeckGenerator(cardRegistry);
        // The registry has finished loading before this bean is created. Warm the derived pool
        // during normal startup, while preserving on-demand loading in the test context.
        if (loadMode == OracleLoadMode.EAGER) {
            generator.initializeCardPool();
        }
        return generator;
    }
}
