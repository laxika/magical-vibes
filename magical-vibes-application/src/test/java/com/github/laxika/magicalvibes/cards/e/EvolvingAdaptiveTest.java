package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvolvingAdaptiveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an oil counter and gets +1/+1 for it")
    void entersWithOilCounterAndScalesFromIt() {
        Permanent adaptive = addEvolvingAdaptive();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, adaptive)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adaptive)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts an oil counter on itself when a bigger creature enters under its controller's control")
    void putsOilCounterWhenBiggerCreatureEnters() {
        Permanent adaptive = addEvolvingAdaptive();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, adaptive)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adaptive)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not put an oil counter on itself when the entering creature is not bigger")
    void doesNotPutOilCounterForEqualCreature() {
        Permanent adaptive = addEvolvingAdaptive();

        harness.setHand(player1, List.of(new EvolvingAdaptive()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    private Permanent addEvolvingAdaptive() {
        harness.setHand(player1, List.of(new EvolvingAdaptive()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Evolving Adaptive");
    }
}
