package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtScour.class})
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

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    // ===== Draw effect =====

    @Test
    @DisplayName("Draws one card for the caster")
    void drawsOneCard() {
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

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

        harness.castAndResolveInstant(player1, 0, player2.getId());

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

        harness.castAndResolveInstant(player1, 0, player1.getId());

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

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Thought Scour");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mills the only remaining card and still draws for the caster")
    void millsShortLibraryAndDraws() {
        ThoughtScour remainingCard = new ThoughtScour();
        harness.setLibrary(player2, List.of(remainingCard));
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty target library does not prevent the caster from drawing")
    void drawsWhenTargetLibraryIsEmpty() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Self-targeting mills the top two cards before drawing the third")
    void millsBeforeDrawingWhenTargetingSelf() {
        ThoughtScour first = new ThoughtScour();
        ThoughtScour second = new ThoughtScour();
        ThoughtScour third = new ThoughtScour();
        ThoughtScour spell = new ThoughtScour();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, spell);
    }
}
