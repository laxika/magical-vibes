package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
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

@CardUsed({ScrollRack.class, ArmoredPegasus.class, LowlandGiant.class})
class ScrollRackTest extends BaseCardTest {

    /** Activates {1}, {T} and lets the ability resolve, leaving the hand-card pick active. */
    private void activateScrollRack() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chosen hand cards are swapped for the same number of cards off the top of the library")
    void swapsChosenHandCardsForLibraryTop() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handPegasus = new ArmoredPegasus();
        Card handGiant = new LowlandGiant();
        Card kept = new ArmoredPegasus();
        harness.setHand(player1, List.of(handPegasus, handGiant, kept));
        List<Card> library = List.of(new LowlandGiant(), new ArmoredPegasus(), new LowlandGiant());
        harness.setLibrary(player1, library);

        activateScrollRack();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(handPegasus.getId(), handGiant.getId()));

        // Two set aside, two moved off the library top, then the "in any order" prompt.
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(kept, library.get(0), library.get(1));
        var reorder = (PendingInteraction.LibraryReorder) gd.interaction.activeInteraction();
        assertThat(reorder.cards()).containsExactly(handPegasus, handGiant);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(handGiant, handPegasus, library.get(2));
    }

    @Test
    @DisplayName("Choosing a single card skips the ordering prompt and puts it straight on top")
    void singleCardGoesStraightOnTop() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handPegasus = new ArmoredPegasus();
        harness.setHand(player1, List.of(handPegasus));
        List<Card> library = List.of(new LowlandGiant(), new ArmoredPegasus());
        harness.setLibrary(player1, library);

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of(handPegasus.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handPegasus, library.get(1));
    }

    @Test
    @DisplayName("Choosing no cards leaves hand and library untouched")
    void choosingNothingDoesNothing() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handPegasus = new ArmoredPegasus();
        harness.setHand(player1, List.of(handPegasus));
        List<Card> library = List.of(new LowlandGiant(), new ArmoredPegasus());
        harness.setLibrary(player1, library);

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handPegasus);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("A library shorter than the number set aside moves only what is there, and all set-aside cards come back")
    void shortLibraryMovesFewerCards() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handPegasus = new ArmoredPegasus();
        Card handGiant = new LowlandGiant();
        harness.setHand(player1, List.of(handPegasus, handGiant));
        List<Card> library = List.of(new ArmoredPegasus());
        harness.setLibrary(player1, library);

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of(handPegasus.getId(), handGiant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handPegasus, handGiant);
    }

    @Test
    @DisplayName("An empty hand resolves with no prompt")
    void emptyHandResolvesSilently() {
        harness.addToBattlefield(player1, new ScrollRack());
        harness.setHand(player1, List.of());
        List<Card> library = List.of(new ArmoredPegasus(), new LowlandGiant());
        harness.setLibrary(player1, library);

        activateScrollRack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }
}
