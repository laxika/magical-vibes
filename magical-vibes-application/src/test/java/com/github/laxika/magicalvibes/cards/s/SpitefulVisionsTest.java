package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpitefulVisionsTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private void drainStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 50) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Each player draws an additional card at their draw step and takes 1 damage per draw")
    void additionalDrawAndDamageAtDrawStep() {
        harness.addToBattlefield(player1, new SpitefulVisions());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        drainStack();

        // Normal draw + additional draw = 2 cards, 1 damage each = 2 damage.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller drawing from a spell takes 1 damage per card drawn")
    void controllerDrawFromSpellTakesDamage() {
        harness.addToBattlefield(player1, new SpitefulVisions());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        drainStack();

        // Counsel draws 2 cards; Spiteful Visions deals 1 to the controller per draw.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent drawing from a spell takes 1 damage per card drawn")
    void opponentDrawFromSpellTakesDamage() {
        harness.addToBattlefield(player1, new SpitefulVisions());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        drainStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
