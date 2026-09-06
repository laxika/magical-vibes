package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamCache.class, Forest.class, Island.class})
class DreamCacheTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(i % 2 == 0 ? new Forest() : new Island());
        }
        return cards;
    }

    private void castDreamCache(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new DreamCache(), "{2}{U}");
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
    @DisplayName("Requires two cards when at least two cards are available")
    void requiresTwoCardsWhenAvailable() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.minCount()).isEqualTo(2);
                    assertThat(choice.maxCount()).isEqualTo(2);
                });
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
