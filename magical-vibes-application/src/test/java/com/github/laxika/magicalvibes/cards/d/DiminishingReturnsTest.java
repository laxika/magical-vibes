package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiminishingReturnsTest extends BaseCardTest {

    private void castDiminishingReturns() {
        harness.setHand(player1, List.of(new DiminishingReturns()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player may draw the full seven cards")
    void eachPlayerDrawsSeven() {
        castDiminishingReturns();

        // Active player (player1) chooses first, then the non-active player.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 7);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 7);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Controller exiles the top ten cards of their library")
    void controllerExilesTopTen() {
        castDiminishingReturns();
        harness.handleXValueChosen(player1, 7);
        harness.handleXValueChosen(player2, 7);

        // Only the controller exiles, and exactly ten cards (the spell itself is not exiled).
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hand and graveyard are shuffled into the library, not lost")
    void handAndGraveyardShuffledIntoLibrary() {
        Card graveyardCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DiminishingReturns()));
        harness.setHand(player2, List.of(handCard));
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard);
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        // The tracked cards left their original zones (shuffled into the library).
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c == graveyardCard);
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c == handCard);
    }

    @Test
    @DisplayName("A player may draw fewer than seven")
    void mayDrawFewerThanSeven() {
        castDiminishingReturns();

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose to draw more than seven")
    void cannotDrawMoreThanSeven() {
        castDiminishingReturns();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 8))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void fillLibrary(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        if (deck == null) {
            deck = new ArrayList<>();
            gd.playerDecks.put(player.getId(), deck);
        }
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
    }
}
