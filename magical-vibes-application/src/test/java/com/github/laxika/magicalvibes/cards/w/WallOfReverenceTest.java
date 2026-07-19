package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfReverenceTest extends BaseCardTest {

    /** Advance to the controller's end step; the MayEffect resolves and awaits the "may" choice. */
    private void advanceToEndStepMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto the stack
        harness.passBothPriorities(); // resolve the MayEffect → may prompt
    }

    @Test
    @DisplayName("Accepting gains life equal to the chosen creature's power")
    void acceptGainsLifeEqualToTargetPower() {
        harness.addToBattlefield(player1, new WallOfReverence());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gained equals the specific chosen creature's power, not another's")
    void gainsChosenCreaturePower() {
        harness.addToBattlefield(player1, new WallOfReverence());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLife(player1, 20);

        advanceToEndStepMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Declining gains no life")
    void declineGainsNoLife() {
        harness.addToBattlefield(player1, new WallOfReverence());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's creature is not a legal target")
    void opponentCreatureNotTargetable() {
        harness.addToBattlefield(player1, new WallOfReverence());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        // Only creatures player1 controls are offered — the opponent's creature is excluded.
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId());
    }
}
