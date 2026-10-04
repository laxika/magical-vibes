package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreeFromFlesh.class, GrizzlyBears.class, Forest.class, HexgoldSlash.class})
class FreeFromFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 and two oil counters")
    void boostsAndAddsOilCounters() {
        Permanent bears = castFreeFromFlesh();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn but oil counters remain")
    void boostWearsOffButCountersRemain() {
        Permanent bears = castFreeFromFlesh();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FreeFromFlesh()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opposing creature receives both the boost and oil counters")
    void canTargetOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FreeFromFlesh()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Free from Flesh");
    }

    @Test
    @DisplayName("Repeated casts accumulate boosts and oil counters")
    void repeatedCastsAccumulate() {
        Permanent bears = castFreeFromFlesh();
        harness.setHand(player1, List.of(new FreeFromFlesh()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(6);
        assertThat(bears.getEffectiveToughness()).isEqualTo(6);
        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost remains during the end step and expires during cleanup")
    void boostLastsThroughEndStep() {
        Permanent bears = castFreeFromFlesh();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Neither effect applies when the creature dies in response")
    void removedTargetReceivesNeitherEffect() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FreeFromFlesh()));
        harness.setHand(player2, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.OIL)).isZero();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Free from Flesh");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFreeFromFlesh() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FreeFromFlesh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        return bears;
    }
}
