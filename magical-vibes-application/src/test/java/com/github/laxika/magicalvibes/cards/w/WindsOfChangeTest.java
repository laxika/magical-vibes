package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindsOfChangeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws the same number of cards as they had in hand")
    void wheelPreservesHandSize() {
        harness.setHand(player1, List.of(new WindsOfChange(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 had 2 cards remaining after casting Winds of Change, so draws 2.
        // Player2 had 2 cards, so draws 2.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Player with empty hand draws zero cards")
    void emptyHandDrawsZero() {
        harness.setHand(player1, List.of(new WindsOfChange()));
        harness.setHand(player2, List.of());

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Original hand cards are shuffled into library, not kept")
    void handCardsGoIntoLibrary() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new WindsOfChange()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player2 shuffled 1 card in and drew 1: hand size stays 1, library size unchanged.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
    }

    private void fillDeck(Player player, int count) {
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
