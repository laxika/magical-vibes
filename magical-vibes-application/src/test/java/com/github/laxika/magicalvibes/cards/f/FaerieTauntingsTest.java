package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieTauntingsTest extends BaseCardTest {

    /** Puts player1 on defense during player2's turn so player1 may cast an instant. */
    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting makes each opponent lose 1 life")
    void acceptDrainsOpponent() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Shock deals 2 to player2, and the trigger drains 1 more.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Declining leaves opponent life unchanged by the trigger")
    void declineLeavesLife() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Only Shock's 2 damage, no drain.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Casting on your own turn does not trigger")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
