package com.github.laxika.magicalvibes.cards.d;

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

class DreamCacheTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(i % 2 == 0 ? new GrizzlyBears() : new Shock());
        }
        return cards;
    }

    private void castDreamCache(List<Card> library) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(library);

        harness.setHand(player1, List.of(new DreamCache()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws three cards, then asks which hand cards to return")
    void drawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        // Top three drawn into hand; the choice interaction is now active.
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
    }

    @Test
    @DisplayName("Choosing two cards for the top puts them on top of the library, first chosen on top")
    void putsChosenCardsOnTop() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.class);

        harness.handleListChoice(player1, "Top");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        // First chosen ends up nearest the top, then the second chosen, then the untouched library.
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawn0, drawn1, library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Choosing two cards for the bottom puts them on the bottom of the library")
    void putsChosenCardsOnBottom() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), drawn0, drawn1);
    }
}
