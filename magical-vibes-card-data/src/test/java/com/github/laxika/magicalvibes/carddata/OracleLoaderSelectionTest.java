package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonSetSource;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallSetSource;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.SetSource;
import com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the {@code oracle.data-provider} property does to the context: it picks the preferred
 * provider only, so the same beans exist whatever it says, and a value naming no provider fails
 * startup.
 *
 * <p>Registers the store, the sources and the loader but not {@link CardRegistry}, so refreshing
 * the context performs no load. Uses a plain {@code AnnotationConfigApplicationContext} on purpose:
 * that is what {@code GameTestEngineContext} boots, so the property has to work without Spring
 * Boot's lenient enum conversion.
 */
class OracleLoaderSelectionTest {

    /** Card tests boot without application.properties, so an absent property must be accepted. */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"SCRYFALL", "MTGJSON", "mtgjson"})
    void everyProviderValueBootsBothSourcesAndTheOneLoader(String providerValue) {
        try (AnnotationConfigApplicationContext context = context(providerValue)) {
            context.refresh();

            assertThat(context.getBeansOfType(SetSource.class).values())
                    .extracting(SetSource::provider)
                    .containsExactlyInAnyOrder(OracleDataProvider.SCRYFALL, OracleDataProvider.MTGJSON);
            assertThat(context.getBean(OracleLoader.class)).isInstanceOf(StoredSetOracleLoader.class);
        }
    }

    /**
     * A typo must not boot the application with some silent default: it would surface much later as
     * sets quietly coming from the provider nobody asked for.
     */
    @Test
    void aTypoedProviderFailsStartup() {
        try (AnnotationConfigApplicationContext context = context("SCYRFALL")) {
            assertThatThrownBy(context::refresh)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasStackTraceContaining("Unknown oracle.data-provider 'SCYRFALL'");
        }
    }

    /**
     * Points the context at exactly the given provider value, or at none for {@code null}.
     *
     * <p>The ambient property sources have to go first. Gradle defaults
     * {@code oracle.data-provider} to {@code MTGJSON} in the test JVM, so a
     * {@code StandardEnvironment} sees that value as a system property. Absence cannot be simulated
     * by adding a property source — a source that resolves the key to nothing is simply skipped, and
     * the system properties underneath answer instead — so the only way to test the missing case is
     * to take those sources away.
     */
    private static AnnotationConfigApplicationContext context(String providerValue) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        if (providerValue != null) {
            sources.addFirst(new MapPropertySource("test", Map.of("oracle.data-provider", providerValue)));
        }
        context.register(ScryfallSetSource.class, MtgjsonSetSource.class, SetDataStore.class,
                StoredSetOracleLoader.class);
        return context;
    }
}
