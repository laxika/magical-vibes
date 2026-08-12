package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrainstormTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(i % 2 == 0 ? new GrizzlyBears() : new Shock());
        }
        return cards;
    }

    private void castBrainstorm(List<Card> library) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(library);

        harness.setHand(player1, List.of(new Brainstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws three cards, then asks which two hand cards to put on top")
    void drawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
    }

    @Test
    @DisplayName("Choosing two cards puts them on top of the library, first chosen on top, no top/bottom prompt")
    void putsChosenCardsOnTop() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));

        // No destination prompt for Brainstorm — the cards go straight to the top.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawn0, drawn1, library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Can put a card that was already in hand on top")
    void choosesFromEntireHand() {
        List<Card> library = fiveCards();
        Card alreadyInHand = new Shock();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(library);

        harness.setHand(player1, List.of(new Brainstorm(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId(), library.get(0).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Puts all available cards back when the library has fewer than three cards")
    void handlesShortLibrary() {
        Card libraryCard = new GrizzlyBears();
        Card alreadyInHand = new Shock();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(libraryCard);

        harness.setHand(player1, List.of(new Brainstorm(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId(), libraryCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(alreadyInHand, libraryCard);
    }
}
