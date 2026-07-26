package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Verifies the set-completeness metric ({@link CardCatalog#getImplementedFraction}) and the 80%
 * eligibility threshold that gates which sets the "All Random" mode offers as a deck source. Pure
 * unit test: it registers a known total rather than loading oracle data, so the implemented count
 * (from the classpath card registrations) is the only real input.
 *
 * <p>JUnit builds a fresh instance per test method, so each case gets its own registry and the
 * totals one test registers cannot be seen by another. This used to need {@code @BeforeEach} and
 * {@code @AfterEach} hooks clearing a JVM-wide map, because any loader test that ran earlier in the
 * same fork left it populated.
 */
class SetCompletenessTest {

    // Any set with implemented cards; only its printing count (not oracle data) matters here.
    private static final CardSet SET = CardSet.SET_SOM;

    private final CardRegistry registry = scannedRegistry();
    private final RandomDeckGenerator randomDeckGenerator = new RandomDeckGenerator(registry);

    /** A registry that scans the classpath for real but whose loader supplies nothing. */
    private static CardRegistry scannedRegistry() {
        CardRegistry registry = new CardRegistry((setCode, implemented) ->
                new SetOracleData(null, 0, Map.of(), Map.of(), Map.of(), Map.of()));
        registry.load();
        return registry;
    }

    @Test
    void implementedFractionIsImplementedOverTotal() {
        int implemented = registry.getPrintings(SET).size();
        assertThat(implemented).isPositive();

        registry.registerSetCardTotal(SET.getCode(), implemented);
        assertThat(registry.getImplementedFraction(SET)).isEqualTo(1.0);

        registry.registerSetCardTotal(SET.getCode(), implemented * 2);
        assertThat(registry.getImplementedFraction(SET)).isCloseTo(0.5, within(1e-9));
    }

    @Test
    void unknownTotalYieldsZeroFractionAndIneligible() {
        // No total registered → denominator unknown.
        assertThat(registry.getImplementedFraction(SET)).isZero();
        assertThat(randomDeckGenerator.isSetRandomEligible(SET)).isFalse();
    }

    @Test
    void eligibilityFlipsAtEightyPercentImplemented() {
        int implemented = registry.getPrintings(SET).size();

        // total = floor(implemented / 0.8) makes the fraction land at or just above 0.80.
        int totalAtThreshold = (int) Math.floor(implemented / 0.80);
        registry.registerSetCardTotal(SET.getCode(), totalAtThreshold);
        assertThat(registry.getImplementedFraction(SET)).isGreaterThanOrEqualTo(0.80);
        assertThat(randomDeckGenerator.isSetRandomEligible(SET)).isTrue();

        // One more card in the set drops it just below 80% → no longer offered.
        registry.registerSetCardTotal(SET.getCode(), totalAtThreshold + 1);
        assertThat(registry.getImplementedFraction(SET)).isLessThan(0.80);
        assertThat(randomDeckGenerator.isSetRandomEligible(SET)).isFalse();
    }
}
