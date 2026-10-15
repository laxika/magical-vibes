package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoulderbornDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonstormForecaster.class, DragonstormGlobe.class, BoulderbornDragon.class})
class DragonstormForecasterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only Dragonstorm Globe and Boulderborn Dragon")
    void offersOnlyNamedCards() {
        activateSearch(List.of(
                new DragonstormGlobe(),
                new BoulderbornDragon(),
                new DragonstormForecaster()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Dragonstorm Globe", "Boulderborn Dragon");
    }

    @Test
    @DisplayName("The chosen named card goes to its controller's hand")
    void chosenCardGoesToHand() {
        activateSearch(List.of(new BoulderbornDragon()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Boulderborn Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability does nothing when neither named card is in the library")
    void noNamedCardFound() {
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateSearch(List.of(new DragonstormForecaster()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void activateSearch(List<Card> library) {
        harness.addToBattlefield(player1, new DragonstormForecaster());
        findPermanent(player1, "Dragonstorm Forecaster").setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }

    @Test
    @DisplayName("Choosing Dragonstorm Globe takes only that card from the controller's library")
    void choosesGlobeWhenBothNamesArePresent() {
        DragonstormGlobe globe = new DragonstormGlobe();
        BoulderbornDragon dragon = new BoulderbornDragon();
        DragonstormGlobe opponentsGlobe = new DragonstormGlobe();
        harness.setLibrary(player2, List.of(opponentsGlobe));
        activateSearch(List.of(globe, dragon));

        assertThat(findPermanent(player1, "Dragonstorm Forecaster").isTapped()).isTrue();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(globe).doesNotContain(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsGlobe);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a named card is available")
    void mayDeclineToFindNamedCard() {
        DragonstormGlobe globe = new DragonstormGlobe();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateSearch(List.of(globe));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(globe);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes without taking a card")
    void emptyLibrarySearchCompletes() {
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateSearch(List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

}
