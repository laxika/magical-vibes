package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpponentThreatEstimatorTest {

    @Test
    @DisplayName("Empty hand returns NONE — no trick possible")
    void emptyHandReturnsNone() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 3);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(0, pool);

        assertThat(est).isEqualTo(OpponentThreatEstimator.ThreatEstimate.NONE);
        assertThat(est.hasThreat()).isFalse();
    }

    @Test
    @DisplayName("No mana returns NONE — can't cast anything")
    void noManaReturnsNone() {
        ManaPool pool = new ManaPool();

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(3, pool);

        assertThat(est).isEqualTo(OpponentThreatEstimator.ThreatEstimate.NONE);
        assertThat(est.hasThreat()).isFalse();
    }

    @Test
    @DisplayName("Green mana with 1 total → pump estimate of 3 (Giant Growth range)")
    void greenManaEstimatesPump3() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 1);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(3);
    }

    @Test
    @DisplayName("Green mana with 3+ total → pump estimate of 4 (Titanic Growth range)")
    void greenManaHighManaEstimatesPump4() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 2);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(4);
    }

    @Test
    @DisplayName("Red mana with 2 total → pump estimate of 3 (Brute Strength range)")
    void redManaEstimatesPump3() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(3);
    }

    @Test
    @DisplayName("Red mana with 1 total → pump estimate of 2 (Infuriate range)")
    void redManaLowEstimatesPump2() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.RED, 1);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(1, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("White mana → pump estimate of 2 (Feat of Resistance range)")
    void whiteManaEstimatesPump2() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.WHITE, 2);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Black mana with 1+ total → pump estimate of 2")
    void blackManaEstimatesPump2() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.BLACK, 1);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless-only with 3 mana → conservative pump estimate of 2")
    void colorlessOnlyEstimatesConservatively() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.COLORLESS, 3);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isTrue();
        assertThat(est.estimatedPumpBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless-only with 1 mana → no pump expected (NONE)")
    void colorlessOnlyLowManaReturnsNone() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.COLORLESS, 1);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        assertThat(est.hasThreat()).isFalse();
    }

    @Test
    @DisplayName("Probability scales with hand size")
    void probabilityScalesWithHandSize() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 2);

        double prob1 = OpponentThreatEstimator.estimate(1, pool).trickProbability();
        double prob3 = OpponentThreatEstimator.estimate(3, pool).trickProbability();
        double prob5 = OpponentThreatEstimator.estimate(5, pool).trickProbability();

        assertThat(prob1).isLessThan(prob3);
        assertThat(prob3).isLessThan(prob5);
        // Cap at 50%
        assertThat(prob5).isLessThanOrEqualTo(0.50);
    }

    @Test
    @DisplayName("Multiple colors → highest pump estimate wins")
    void multipleColorsPicksHighestPump() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 1);  // green at 1 mana → 3
        pool.add(ManaColor.WHITE, 1);  // white at 2 total → 2

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(2, pool);

        // Green at 2 total mana → 3, white → 2; max = 3
        assertThat(est.estimatedPumpBoost()).isEqualTo(3);
    }

    @Test
    @DisplayName("Probability cap: 7-card hand should not exceed 50%")
    void probabilityCappedAt50Percent() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.GREEN, 3);

        OpponentThreatEstimator.ThreatEstimate est = OpponentThreatEstimator.estimate(7, pool);

        assertThat(est.trickProbability()).isEqualTo(0.50);
    }
}
