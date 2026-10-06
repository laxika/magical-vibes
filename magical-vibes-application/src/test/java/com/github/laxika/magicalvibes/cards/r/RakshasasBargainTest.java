package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakshasasBargain.class, GrizzlyBears.class, Shock.class})
class RakshasasBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters library reveal choice for the top four cards")
    void resolvingEntersRevealChoiceState() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

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
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

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
        harness.setLibrary(player1, List.of(card));

        castRakshasasBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Must choose two cards when at least two are available")
    void mustChooseTwoCards() {
        Card first = new RakshasasBargain();
        Card second = new RakshasasBargain();
        Card third = new RakshasasBargain();
        harness.setLibrary(player1, List.of(first, second, third));

        castRakshasasBargain();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
    }

    @Test
    @DisplayName("With two cards both go into hand without a choice")
    void twoCardLibraryGoesToHand() {
        Card first = new RakshasasBargain();
        Card second = new RakshasasBargain();
        harness.setLibrary(player1, List.of(first, second));

        castRakshasasBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rakshasa's Bargain");
    }

    @Test
    @DisplayName("An empty library resolves without drawing or requiring a choice")
    void emptyLibraryResolves() {
        harness.setLibrary(player1, List.of());

        castRakshasasBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rakshasa's Bargain");
    }

    private void castRakshasasBargain() {
        harness.setHand(player1, List.of(new RakshasasBargain()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0);
    }

}
