package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvolvingAdaptive.class, GrizzlyBears.class, CopperLonglegs.class})
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
        resolveAllTriggers();

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

    @Test
    void growsWhenOnlyEnteringCreaturesToughnessIsGreater() {
        Permanent adaptive = addEvolvingAdaptive();

        harness.setHand(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, adaptive)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adaptive)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        Permanent adaptive = addEvolvingAdaptive();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CopperLonglegs()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksSizeComparisonWhenTriggerResolves() {
        Permanent adaptive = addEvolvingAdaptive();

        harness.setHand(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        adaptive.setCounterCount(CounterType.OIL, 3);
        resolveAllTriggers();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    private Permanent addEvolvingAdaptive() {
        harness.setHand(player1, List.of(new EvolvingAdaptive()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Evolving Adaptive");
    }
}
