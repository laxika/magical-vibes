package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Verifies the set-completeness metric ({@link CardSet#getImplementedFraction()}) and the 80%
 * eligibility threshold that gates which sets the "All Random" mode offers as a deck source. Pure
 * unit test: it registers a known total rather than loading oracle data, so the implemented count
 * (from the classpath card registrations) is the only real input.
 */
class SetCompletenessTest {

    // Any set with implemented cards; only its printing count (not oracle data) matters here.
    private static final CardSet SET = CardSet.SET_SOM;

    @AfterEach
    void tearDown() {
        // Undo the totals this test registered so it can't perturb a later test's read.
        CardSet.clearSetCardTotalRegistry();
    }

    @Test
    void implementedFractionIsImplementedOverTotal() {
        int implemented = SET.getPrintings().size();
        assertThat(implemented).isPositive();

        CardSet.registerSetCardTotal(SET.getCode(), implemented);
        assertThat(SET.getImplementedFraction()).isEqualTo(1.0);

        CardSet.registerSetCardTotal(SET.getCode(), implemented * 2);
        assertThat(SET.getImplementedFraction()).isCloseTo(0.5, within(1e-9));
    }

    @Test
    void unknownTotalYieldsZeroFractionAndIneligible() {
        // No total registered (registry cleared) → denominator unknown.
        assertThat(SET.getImplementedFraction()).isZero();
        assertThat(RandomDeckGenerator.isSetRandomEligible(SET)).isFalse();
    }

    @Test
    void eligibilityFlipsAtEightyPercentImplemented() {
        int implemented = SET.getPrintings().size();

        // total = floor(implemented / 0.8) makes the fraction land at or just above 0.80.
        int totalAtThreshold = (int) Math.floor(implemented / 0.80);
        CardSet.registerSetCardTotal(SET.getCode(), totalAtThreshold);
        assertThat(SET.getImplementedFraction()).isGreaterThanOrEqualTo(0.80);
        assertThat(RandomDeckGenerator.isSetRandomEligible(SET)).isTrue();

        // One more card in the set drops it just below 80% → no longer offered.
        CardSet.registerSetCardTotal(SET.getCode(), totalAtThreshold + 1);
        assertThat(SET.getImplementedFraction()).isLessThan(0.80);
        assertThat(RandomDeckGenerator.isSetRandomEligible(SET)).isFalse();
    }
}
