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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("May return cards that were already in hand before drawing")
    void returnsPreviouslyHeldCards() {
        List<Card> library = fiveCards();
        Card heldForest = new Forest();
        Card heldIsland = new Island();
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new DreamCache(), "{2}{U}");
        harness.setHand(player1, List.of(heldForest, heldIsland));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(heldIsland.getId(), heldForest.getId()));
        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(heldIsland, heldForest, library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows the controller to choose the opposite order of the returned cards")
    void returnsCardsInChosenOrder() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(2).getId(), library.get(0).getId()));
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(1));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), library.get(2), library.get(0));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot select the same card twice instead of returning two distinct cards")
    void rejectsDuplicateCardSelection() {
        List<Card> library = fiveCards();
        castDreamCache(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3), library.get(4));
    }
}
