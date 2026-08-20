package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RakshasasBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters library reveal choice for the top four cards")
    void resolvingEntersRevealChoiceState() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        castRakshasasBargain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing two cards puts them into hand and the rest into the graveyard")
    void choosingTwoPutsRestInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        castRakshasasBargain();
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId(), card3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1, card3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card0, card2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rakshasa's Bargain");
    }

    @Test
    @DisplayName("With fewer than two cards in the library, all cards go into hand")
    void smallLibraryGoesToHand() {
        Card card = new GrizzlyBears();
        setupTopCards(List.of(card));

        castRakshasasBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castRakshasasBargain() {
        harness.setHand(player1, List.of(new RakshasasBargain()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
