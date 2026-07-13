package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyggRiverCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent lost 3+ life this turn: at end step the controller may draw a card")
    void opponentLostThreeLifeMayDraw() {
        harness.addToBattlefield(player1, new SyggRiverCutthroat());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        // Two Shocks make the opponent lose 4 life (damage causes loss of life).
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void declineDrawsNothing() {
        harness.addToBattlefield(player1, new SyggRiverCutthroat());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent lost fewer than 3 life: ability does not trigger")
    void opponentLostTwoLifeNoTrigger() {
        harness.addToBattlefield(player1, new SyggRiverCutthroat());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        // A single Shock is only 2 life lost — below the threshold.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only opponents count: the controller losing 3 life does not trigger it")
    void controllerLifeLossDoesNotTrigger() {
        harness.addToBattlefield(player1, new SyggRiverCutthroat());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        // The controller loses 4 life this turn; no opponent lost any.
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    /** Advances into the given player's end step, stopping at the Sygg may prompt if it fires. */
    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 8; i++) {
            if (gd.interaction.isAwaitingInput()) break;
            harness.passBothPriorities();
            if (gd.currentStep == TurnStep.END_STEP && gd.stack.isEmpty()
                    && !gd.interaction.isAwaitingInput()) {
                break;
            }
        }
    }
}
