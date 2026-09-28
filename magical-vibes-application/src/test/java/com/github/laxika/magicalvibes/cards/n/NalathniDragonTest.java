package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NalathniDragon.class)
class NalathniDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times, with each activation adding +1/+0")
    void canActivateMultipleTimes() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four times")
    void sacrificedWhenActivatedFourTimes() {
        addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nalathni Dragon");
        harness.assertInGraveyard(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Activating four times during the end step waits for the next end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        addCreatureReady(player1, new NalathniDragon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nalathni Dragon");
        harness.assertInGraveyard(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }
}
