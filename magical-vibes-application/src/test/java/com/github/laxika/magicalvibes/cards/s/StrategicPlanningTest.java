package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrategicPlanningTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        cast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a card puts it in hand and the rest into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
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
    @DisplayName("Remaining cards do not stay in library")
    void remainingCardsNotInLibrary() {
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With 1 card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
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
        harness.setHand(player1, List.of(new StrategicPlanning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
