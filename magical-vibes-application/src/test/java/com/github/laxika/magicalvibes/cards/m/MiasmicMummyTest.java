package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MiasmicMummyTest extends BaseCardTest {

    // "When this creature enters, each player discards a card."

    private void castMiasmicMummy() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }

    @Test
    @DisplayName("ETB makes each player discard a card, active player first (APNAP)")
    void eachPlayerDiscards() {
        harness.setHand(player1, new ArrayList<>(List.of(new MiasmicMummy(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new HillGiant())));

        castMiasmicMummy();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Active player (player1) discards first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // Then the opponent discards.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("A player with an empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, new ArrayList<>(List.of(new MiasmicMummy())));
        harness.setHand(player2, new ArrayList<>(List.of(new HillGiant())));

        castMiasmicMummy();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player1's hand is empty after casting, so only player2 is prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
