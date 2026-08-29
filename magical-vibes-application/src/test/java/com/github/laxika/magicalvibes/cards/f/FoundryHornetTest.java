package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryHornetTest extends BaseCardTest {

    @Test
    @DisplayName("ETB weakens opposing creatures when you control a creature with a +1/+1 counter")
    void etbWeakensOpposingCreaturesWithCounteredCreature() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFoundryHornet();
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    private void castFoundryHornet() {
        harness.setHand(player1, List.of(new FoundryHornet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
