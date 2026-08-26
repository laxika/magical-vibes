package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InitiatesOfTheEbonHand.class})
class InitiatesOfTheEbonHandTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability adds {B}")
    void activatingAddsBlackMana() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Initiates of the Ebon Hand");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four or more times")
    void sacrificedWhenActivatedFourOrMoreTimes() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Initiates of the Ebon Hand");
        harness.assertInGraveyard(player1, "Initiates of the Ebon Hand");
    }

    @Test
    @DisplayName("Sacrifices at end step after five activations")
    void sacrificedWhenActivatedMoreThanFourTimes() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Initiates of the Ebon Hand");
        harness.assertInGraveyard(player1, "Initiates of the Ebon Hand");
    }

    @Test
    @DisplayName("Activation during the end step sacrifices at the next end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        addReadyInitiates(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Initiates of the Ebon Hand");
        harness.assertInGraveyard(player1, "Initiates of the Ebon Hand");
    }

    private void addReadyInitiates(Player player) {
        addCreatureReady(player, new InitiatesOfTheEbonHand());
    }
}
