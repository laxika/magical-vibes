package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralMemories.class, Island.class})
class AncestralMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(sevenCards());

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing two cards puts them into hand and the other five into the graveyard")
    void choosingTwoPutsTwoInHandFiveInGraveyard() {
        List<Card> cards = sevenCards();
        setupTopCards(cards);

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Card chosen0 = cards.get(0);
        Card chosen1 = cards.get(3);
        harness.handleMultipleCardsChosen(player1, List.of(chosen0.getId(), chosen1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen0, chosen1);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        // The five unchosen cards land in the graveyard (plus Ancestral Memories itself)
        for (Card c : cards) {
            if (!c.getId().equals(chosen0.getId()) && !c.getId().equals(chosen1.getId())) {
                assertThat(graveyard).contains(c);
            }
        }
        assertThat(graveyard).noneMatch(c -> c.getId().equals(chosen0.getId()));
        assertThat(graveyard).noneMatch(c -> c.getId().equals(chosen1.getId()));
    }

    @Test
    @DisplayName("Requires choosing two cards when at least two cards are available")
    void requiresChoosingTwoCardsWhenAvailable() {
        List<Card> cards = sevenCards();
        setupTopCards(cards);

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(cards.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid number of cards selected");
    }

    @Test
    @DisplayName("Choosing clears the awaiting state and empties the revealed cards from the library")
    void choosingClearsStateAndLibrary() {
        List<Card> cards = sevenCards();
        setupTopCards(cards);

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(cards.get(0).getId(), cards.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With only two cards in library, both go directly to hand (no choice needed)")
    void twoCardsInLibraryBothGoToHand() {
        GameData gd = harness.getGameData();
        Card cardA = new Island();
        Card cardB = new Island();
        harness.setLibrary(player1, List.of(cardA, cardB));

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardA, cardB);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With empty library, nothing is drawn")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new AncestralMemories(), "{2}{U}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private List<Card> sevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
