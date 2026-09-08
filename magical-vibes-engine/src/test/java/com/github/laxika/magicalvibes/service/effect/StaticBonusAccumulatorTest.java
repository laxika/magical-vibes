package com.github.laxika.magicalvibes.service.effect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaticBonusAccumulatorTest {

    @Test
    void unsetBasePowerToughnessProducesNonOverriddenStaticBonus() {
        var bonus = new StaticBonusAccumulator().toStaticBonus(0, 0, false);

        assertThat(bonus.basePTOverridden()).isFalse();
        assertThat(bonus.basePowerOverride()).isZero();
        assertThat(bonus.baseToughnessOverride()).isZero();
    }
}
