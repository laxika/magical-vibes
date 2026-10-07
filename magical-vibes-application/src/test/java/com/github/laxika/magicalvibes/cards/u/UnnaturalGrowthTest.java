package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnnaturalGrowth.class, GrizzlyBears.class})
class UnnaturalGrowthTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Doubles power and toughness of each creature you control")
    void doublesOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double opponent's creatures")
    void doesNotDoubleOpponentCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers during opponent's combat as well")
    void triggersOnOpponentsCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void multipleCopiesDoubleIndependently() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
    }

    @Test
    void anotherCombatInSameTurnDoublesAlreadyBoostedStats() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);
        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
    }

    @Test
    void includesCountersAtResolutionButDoesNotDoubleLaterCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new UnnaturalGrowth());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    void affectsCreaturesPresentAtResolutionOnly() {
        harness.addToBattlefield(player1, new UnnaturalGrowth());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }
}
