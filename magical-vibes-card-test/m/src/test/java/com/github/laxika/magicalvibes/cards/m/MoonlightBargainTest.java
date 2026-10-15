package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodletterOfAclazotz;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonlightBargain.class, GrayscaledGharial.class, BloodletterOfAclazotz.class})
class MoonlightBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying for selected cards puts them into hand and the rest into the graveyard")
    void payingForSelectedCards() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        Card card2 = new GrayscaledGharial();
        Card card3 = new GrayscaledGharial();
        Card card4 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4));

        castMoonlightBargain();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card3.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId(), card3.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card1.getId(), card2.getId(), card4.getId());
    }

    @Test
    @DisplayName("A controller who cannot pay puts all revealed cards into the graveyard")
    void cannotPayForAnyCard() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        Card card2 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1, card2));
        harness.setLife(player1, 1);

        castMoonlightBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card0.getId(), card1.getId(), card2.getId());
    }

    @Test
    @DisplayName("A controller cannot choose more cards than they can pay for")
    void cannotOverpayForSelectedCards() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        Card card2 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1, card2));
        harness.setLife(player1, 3);

        castMoonlightBargain();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(card0.getId(), card1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId());
    }

    @Test
    @DisplayName("Declining the optional choice puts all revealed cards into the graveyard")
    void decliningSelection() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        Card card2 = new GrayscaledGharial();
        Card card3 = new GrayscaledGharial();
        Card card4 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4));

        castMoonlightBargain();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card0.getId(), card1.getId(), card2.getId(), card3.getId(), card4.getId());
    }

    @Test
    @DisplayName("Only the top five cards can be purchased")
    void purchasingAllFiveLeavesSixthCardInLibrary() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        Card card2 = new GrayscaledGharial();
        Card card3 = new GrayscaledGharial();
        Card card4 = new GrayscaledGharial();
        Card card5 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4, card5));

        castMoonlightBargain();
        harness.handleMultipleCardsChosen(player1,
                List.of(card0.getId(), card1.getId(), card2.getId(), card3.getId(), card4.getId()));

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId(), card1.getId(), card2.getId(), card3.getId(), card4.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card5.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(card0.getId(), card1.getId(), card2.getId(), card3.getId(), card4.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library requires no payment or choice")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        castMoonlightBargain();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Moonlight Bargain");
    }

    @Test
    @DisplayName("Separate payments must remain affordable after doubled life loss")
    void doubledLifeLossCannotEnableAnUnaffordableSecondPayment() {
        Card card0 = new GrayscaledGharial();
        Card card1 = new GrayscaledGharial();
        harness.setLibrary(player1, List.of(card0, card1));
        harness.setLife(player1, 5);
        harness.addToBattlefield(player2, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player2);

        castMoonlightBargain();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(card0.getId(), card1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card1.getId());
    }
    private void castMoonlightBargain() {
        harness.castFromHand(player1, new MoonlightBargain(), "{3}{B}{B}");
        harness.passBothPriorities();
    }
}
