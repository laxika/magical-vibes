package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallOracleLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Which loader the {@code oracle.data-provider} property selects, and what happens when it selects
 * none.
 *
 * <p>Asserts on <em>bean definitions</em> rather than beans: the surviving loader would perform the
 * real network-or-cache load in its {@code @PostConstruct}, so these tests register without
 * refreshing. Registration is enough — {@code @ConditionalOnProperty} is evaluated when the
 * definition is registered, not when the bean is created. The one test that does refresh is the
 * typo case, where by construction no loader survives to be instantiated.
 *
 * <p>Uses a plain {@code AnnotationConfigApplicationContext} on purpose: that is what
 * {@code GameTestEngineContext} boots, so the conditional has to work without Spring Boot.
 */
class OracleLoaderSelectionTest {

    private static final String SCRYFALL_BEAN = "scryfallOracleLoader";
    private static final String MTGJSON_BEAN = "mtgjsonOracleLoader";

    /** Card tests boot without application.properties, so absent must mean Scryfall. */
    @Test
    void anAbsentPropertySelectsScryfall() {
        assertThat(loaderBeansSelectedBy(null)).containsExactly(SCRYFALL_BEAN);
    }

    @Test
    void scryfallSelectsOnlyTheScryfallLoader() {
        assertThat(loaderBeansSelectedBy("SCRYFALL")).containsExactly(SCRYFALL_BEAN);
    }

    /** CI passes -Doracle.data-provider=MTGJSON; it must deselect Scryfall, not merely add MTGJSON. */
    @Test
    void mtgjsonSelectsOnlyTheMtgjsonLoader() {
        assertThat(loaderBeansSelectedBy("MTGJSON")).containsExactly(MTGJSON_BEAN);
    }

    /** havingValue matches case-insensitively, so the enum-cased values above are not load-bearing. */
    @Test
    void theProviderValueIsMatchedCaseInsensitively() {
        assertThat(loaderBeansSelectedBy("mtgjson")).containsExactly(MTGJSON_BEAN);
    }

    /**
     * A conditional that matches nothing is silent on its own, so a typo would otherwise boot the
     * application with an empty oracle registry and surface much later as nameless, costless cards.
     * {@link CardRegistry} takes the loader as a required constructor dependency, which turns that
     * into an ordinary unsatisfied-dependency failure at context refresh.
     */
    @Test
    void aTypoedProviderFailsStartupInsteadOfLoadingNoOracleDataAtAll() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            withProvider(context, "SCYRFALL");
            context.register(ScryfallOracleLoader.class, MtgjsonOracleLoader.class, CardRegistry.class);

            // Safe to refresh only because no loader survives the conditional: CardRegistry fails
            // to construct, so nothing ever reaches the real scan-and-load.
            assertThatThrownBy(context::refresh)
                    .rootCause()
                    .isInstanceOf(NoSuchBeanDefinitionException.class)
                    .hasMessageContaining(OracleLoader.class.getSimpleName());
        }
    }

    /** Loader bean definitions surviving the conditional for the given property value. */
    private static Set<String> loaderBeansSelectedBy(String providerValue) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            withProvider(context, providerValue);
            context.register(ScryfallOracleLoader.class, MtgjsonOracleLoader.class);

            // Deliberately no refresh() — see the class javadoc.
            return Arrays.stream(context.getBeanDefinitionNames())
                    .filter(name -> name.equals(SCRYFALL_BEAN) || name.equals(MTGJSON_BEAN))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Points the context at exactly the given provider value, or at none for {@code null}.
     *
     * <p>The ambient property sources have to go first. Gradle forwards {@code -Doracle.data-provider}
     * into the test JVM and CI passes {@code MTGJSON}, so a {@code StandardEnvironment} sees that
     * value as a system property. Absence cannot be simulated by adding a property source — a source
     * that resolves the key to nothing is simply skipped, and the system properties underneath answer
     * instead — so the only way to test the missing case is to take those sources away.
     */
    private static void withProvider(AnnotationConfigApplicationContext context, String providerValue) {
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        if (providerValue != null) {
            sources.addFirst(new MapPropertySource("test", Map.of("oracle.data-provider", providerValue)));
        }
    }
}
