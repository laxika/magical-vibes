package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisImpTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing out during the controller's untap step makes them discard a card")
    void phasesOutAndDiscards() {
        Permanent imp = addToBattlefield(player1, new TeferisImp());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceTurn(); // player2's turn
        advanceTurn(); // back to player1's untap step — the Imp phases out

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(imp);

        harness.passBothPriorities(); // resolve the phase-out trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phasing back in makes the controller draw a card")
    void phasesInAndDraws() {
        addToBattlefield(player1, new TeferisImp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));

        advanceTurn();
        advanceTurn(); // phases out — no cards in hand, so nothing is discarded
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn(); // phases in

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Teferi's Imp"));

        int handSizeBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve the phase-in trigger

        // One card from the phase-in trigger, one from the draw step that follows.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeTrigger + 2);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addToBattlefield(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
