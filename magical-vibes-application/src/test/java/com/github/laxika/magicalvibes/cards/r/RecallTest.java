package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecallTest extends BaseCardTest {

    @Test
    @DisplayName("Discard X cards, then return that many from graveyard to hand, and exile Recall")
    void discardsThenReturnsThenExiles() {
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new Recall(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3); // {X}{X}{U} with X=1 => 3 mana

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // Recall resolves and asks the controller to discard one card.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // The discarded card is now in the graveyard; the controller returns one card from it.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0); // return Forest

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Recall is exiled, not put into the graveyard.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recall"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Recall"));
    }

    @Test
    @DisplayName("Only min(X, hand size) cards are discarded and returned")
    void returnCountCappedByHandSize() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Recall(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 7); // X=3 => 7 mana

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        // Hand had two cards after casting; both are discarded (X exceeds hand size).
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Two cards were discarded, so exactly two graveyard returns are queued.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recall"));
    }

    @Test
    @DisplayName("X=0 discards and returns nothing but still exiles Recall")
    void zeroXDiscardsNothing() {
        harness.setHand(player1, List.of(new Recall(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1); // X=0 => {U}

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recall"));
    }
}
