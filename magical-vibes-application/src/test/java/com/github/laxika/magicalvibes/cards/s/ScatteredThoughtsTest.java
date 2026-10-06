package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScatteredThoughts.class, DoomedDissenter.class, Syncopate.class})
class ScatteredThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing two cards puts them into hand and the rest into the graveyard")
    void choosingTwoPutsRestInGraveyard() {
        Card card0 = new DoomedDissenter();
        Card card1 = new Syncopate();
        Card card2 = new DoomedDissenter();
        Card card3 = new Syncopate();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        castScatteredThoughts();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId(), card3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1, card3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card0, card2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Scattered Thoughts");
    }

    @Test
    @DisplayName("With fewer than two cards in the library, all cards go into hand")
    void smallLibraryGoesToHand() {
        Card card = new DoomedDissenter();
        harness.setLibrary(player1, List.of(card));

        castScatteredThoughts();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Cannot choose fewer than two cards when at least two are available")
    void mustChooseTwoCards(int selectedCount) {
        Card card0 = new DoomedDissenter();
        Card card1 = new Syncopate();
        harness.setLibrary(player1, List.of(card0, card1, new DoomedDissenter(), new Syncopate()));

        castScatteredThoughts();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(card0.getId()).subList(0, selectedCount)))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card0, card1);
    }

    @Test
    @DisplayName("With three library cards, two go into hand and one into the graveyard")
    void threeCardLibraryStillRequiresChoosingTwo() {
        Card card0 = new DoomedDissenter();
        Card card1 = new Syncopate();
        Card card2 = new DoomedDissenter();
        harness.setLibrary(player1, List.of(card0, card1, card2));

        castScatteredThoughts();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card0, card2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card1).doesNotContain(card0, card2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Scattered Thoughts");
    }

    @Test
    @DisplayName("Exactly two library cards both go into hand without a choice")
    void twoCardLibraryGoesToHand() {
        Card card0 = new DoomedDissenter();
        Card card1 = new Syncopate();
        harness.setLibrary(player1, List.of(card0, card1));

        castScatteredThoughts();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card0, card1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Scattered Thoughts");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card0, card1);
    }

    @Test
    @DisplayName("An empty library does not cause a failed draw")
    void emptyLibraryResolvesWithoutDrawing() {
        harness.setLibrary(player1, List.of());

        castScatteredThoughts();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Scattered Thoughts");
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Only the top four cards are moved, preserving deeper library order")
    void deeperLibraryCardsRemainInOrder() {
        Card card0 = new DoomedDissenter();
        Card card1 = new Syncopate();
        Card card2 = new DoomedDissenter();
        Card card3 = new Syncopate();
        Card card4 = new DoomedDissenter();
        Card card5 = new Syncopate();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4, card5));

        castScatteredThoughts();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card0, card3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card1, card2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card4, card5);
    }

    private void castScatteredThoughts() {
        harness.setHand(player1, List.of(new ScatteredThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }

}
