package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoundReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("At end step, opponent loses life equal to life lost this turn (damage counts)")
    void doublesLifeLostToDamage() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        // Shock deals 2 damage to the opponent — damage causes loss of life.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        resolveEndStep(player1);

        // Wound Reflection: opponent loses another 2 (the life they lost this turn).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Opponent who lost no life this turn loses nothing")
    void noLifeLostNoEffect() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller is unaffected even if the controller lost life this turn")
    void controllerNotAffected() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // The controller takes 2 damage this turn.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        resolveEndStep(player1);

        // Only opponents lose life — the controller stays at 18, opponent untouched.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's turn")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        // On the opponent's own turn they lose life, then Wound Reflection still fires at end step.
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        resolveEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    /** Advances into the given player's end step and resolves the Wound Reflection trigger. */
    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 6; i++) {
            harness.passBothPriorities();
            if (gd.currentStep == TurnStep.END_STEP && gd.stack.isEmpty()
                    && !gd.interaction.isAwaitingInput()) {
                break;
            }
        }
    }
}
