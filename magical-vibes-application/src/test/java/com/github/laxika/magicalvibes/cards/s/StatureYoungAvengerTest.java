package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
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

@CardUsed({StatureYoungAvenger.class, GrizzlyBears.class, JacesIngenuity.class})
class StatureYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell sets Stature's base power and toughness to 4/4")
    void noncreatureSpellSetsBasePowerAndToughness() {
        Permanent stature = addStature();

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent stature = addStature();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base power and toughness setting wears off at end of turn")
    void basePowerAndToughnessSettingWearsOffAtEndOfTurn() {
        Permanent stature = addStature();

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stature changes only when the trigger resolves, before the triggering spell")
    void triggerResolvesBeforeSpell() {
        Permanent stature = addStature();
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(JacesIngenuity.class);
        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's noncreature spell changes only their own Stature")
    void opponentsSpellDoesNotTriggerOurStature() {
        Permanent stature = addStature();
        Permanent opposingStature = harness.addToBattlefieldAndReturn(player2, new StatureYoungAvenger());
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingStature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingStature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The controller's noncreature spell triggers during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        Permanent stature = addStature();
        gd.activePlayerId = player2.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated triggers set the same base values and preserve counters")
    void repeatedTriggersPreserveCountersWithoutStackingBoosts() {
        Permanent stature = addStature();
        stature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new JacesIngenuity(), new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stature)).isEqualTo(5);
        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addStature() {
        return harness.addToBattlefieldAndReturn(player1, new StatureYoungAvenger());
    }
}
