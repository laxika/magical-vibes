package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class SacredGroundTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput() && guard++ < 20) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Opponent's spell destroying your land returns it to the battlefield")
    void opponentDestroysYourLandReturnsIt() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.castSorcery(player2, 0, mountainId);
        resolveStack();

        // Sacred Ground returned the land to its owner's battlefield.
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Your own spell destroying your own land does not trigger Sacred Ground")
    void ownSpellDestroyingOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.castSorcery(player1, 0, mountainId);
        resolveStack();

        // Cause is controlled by the land's owner, so the land stays in the graveyard.
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Opponent destroying their own land does not trigger your Sacred Ground")
    void opponentDestroyingTheirOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player2, 0, mountainId);
        resolveStack();

        // The land went to the opponent's graveyard, not the Sacred Ground controller's, so no trigger.
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }
}
