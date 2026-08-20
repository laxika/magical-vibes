package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonstormForecasterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only Dragonstorm Globe and Boulderborn Dragon")
    void offersOnlyNamedCards() {
        activateSearch(List.of(
                namedCard("Dragonstorm Globe"),
                namedCard("Boulderborn Dragon"),
                namedCard("Grizzly Bears")));

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
        activateSearch(List.of(namedCard("Boulderborn Dragon")));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Boulderborn Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability does nothing when neither named card is in the library")
    void noNamedCardFound() {
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateSearch(List.of(namedCard("Grizzly Bears")));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void activateSearch(List<Card> library) {
        harness.addToBattlefield(player1, new DragonstormForecaster());
        findPermanent(player1, "Dragonstorm Forecaster").setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(library);

        harness.activateAbility(player1, 0, null, null);
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
