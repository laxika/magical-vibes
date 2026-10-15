package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementOfFerocity;
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

@CardUsed({FoundryHornet.class, GrizzlyBears.class, ImplementOfFerocity.class})
class FoundryHornetTest extends BaseCardTest {

    @Test
    @DisplayName("ETB weakens opposing creatures when you control a creature with a +1/+1 counter")
    void etbWeakensOpposingCreaturesWithCounteredCreature() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFoundryHornet();
        resolveAllTriggers();

        assertThat(gqs.getEffectiveToughness(gd, counteredCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not trigger without a creature with a +1/+1 counter")
    void etbDoesNotTriggerWithoutCounteredCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFoundryHornet();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does nothing if the counter condition is lost before resolution")
    void etbDoesNothingIfCounterConditionIsLost() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFoundryHornet();
        harness.passBothPriorities();
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB debuff wears off at the end of the turn")
    void etbDebuffWearsOffAtEndOfTurn() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFoundryHornet();
        resolveAllTriggers();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters on a noncreature and other counter types do not enable the trigger")
    void onlyPlusOnePlusOneCountersOnCreaturesEnableTrigger() {
        Permanent artifact = new Permanent(new ImplementOfFerocity());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        Permanent ownCreature = addCreatureReady(player1, new FoundryHornet());
        ownCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new FoundryHornet());

        castFoundryHornet();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's countered creature does not satisfy the condition")
    void opposingCounteredCreatureDoesNotEnableTrigger() {
        Permanent opposingCreature = addCreatureReady(player2, new FoundryHornet());
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castFoundryHornet();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Adding a counter after entry does not create a missed trigger")
    void counterAddedAfterEntryDoesNotEnableTrigger() {
        Permanent ownCreature = addCreatureReady(player1, new FoundryHornet());
        Permanent opposingCreature = addCreatureReady(player2, new FoundryHornet());

        castFoundryHornet();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A different countered creature can satisfy the condition on resolution")
    void differentCreatureCanSatisfyResolutionCondition() {
        Permanent originalCreature = addCreatureReady(player1, new FoundryHornet());
        originalCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent replacementCreature = addCreatureReady(player1, new FoundryHornet());
        Permanent opposingCreature = addCreatureReady(player2, new FoundryHornet());

        castFoundryHornet();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        originalCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        replacementCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, originalCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff affects creatures present on resolution, not later arrivals")
    void debuffLocksInCreaturesOnResolution() {
        Permanent ownCreature = addCreatureReady(player1, new FoundryHornet());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new FoundryHornet());

        castFoundryHornet();
        harness.passBothPriorities();
        Permanent creatureBeforeResolution = addCreatureReady(player2, new FoundryHornet());
        resolveAllTriggers();
        Permanent creatureAfterResolution = addCreatureReady(player2, new FoundryHornet());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureBeforeResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creatureBeforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureAfterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creatureAfterResolution)).isEqualTo(3);
    }

    private void castFoundryHornet() {
        harness.setHand(player1, List.of(new FoundryHornet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
