package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrategicPlanning.class, Island.class})
class StrategicPlanningTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new Island(), new Island(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Requires choosing one card when cards are available")
    void requiresChoosingOneCardWhenCardsAreAvailable() {
        setupTopCards(List.of(new Island(), new Island(), new Island()));
        cast();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid number of cards selected");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a card puts it in hand and the rest into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new Island();
        Card card1 = new Island();
        Card card2 = new Island();
        setupTopCards(List.of(card0, card1, card2));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card0);
        assertThat(graveyard).contains(card2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With 2 cards, one goes to hand and the other into graveyard")
    void twoCardsOneInHandRestInGraveyard() {
        Card card0 = new Island();
        Card card1 = new Island();
        setupTopCards(List.of(card0, card1));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card0);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Remaining cards do not stay in library")
    void remainingCardsNotInLibrary() {
        Card card0 = new Island();
        Card card1 = new Island();
        Card card2 = new Island();
        setupTopCards(List.of(card0, card1, card2));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards beyond the top three remain in the library")
    void cardsBeyondTopThreeRemainInLibrary() {
        Card card0 = new Island();
        Card card1 = new Island();
        Card card2 = new Island();
        Card card3 = new Island();
        setupTopCards(List.of(card0, card1, card2, card3));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card3);
    }

    @Test
    @DisplayName("With 1 card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new Island();
        gd.playerDecks.get(player1.getId()).add(singleCard);

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void cast() {
        harness.castFromHand(player1, new StrategicPlanning(), "{1}{U}");
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
