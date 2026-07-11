package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtScourTest extends BaseCardTest {

    // ===== Mill effect =====

    @Test
    @DisplayName("Mills two cards from target player's library")
    void millsTwoCards() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    // ===== Draw effect =====

    @Test
    @DisplayName("Draws one card for the caster")
    void drawsOneCard() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Both mill and draw happen when targeting opponent")
    void bothEffectsHappen() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> opponentDeck = gd.playerDecks.get(player2.getId());
        while (opponentDeck.size() > 10) {
            opponentDeck.removeFirst();
        }

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Can target yourself for mill")
    void canTargetSelfForMill() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // 10 - 2 milled - 1 drawn = 7 remaining in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        // 2 milled cards + Thought Scour itself in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Thought Scour goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thought Scour"));
        assertThat(gd.stack).isEmpty();
    }
}
