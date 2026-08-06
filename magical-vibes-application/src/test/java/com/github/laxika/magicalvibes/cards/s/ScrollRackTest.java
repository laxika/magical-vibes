package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrollRackTest extends BaseCardTest {

    private List<Card> setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
        return List.of(cards);
    }

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
        Card handBears = new GrizzlyBears();
        Card handThopter = new Ornithopter();
        Card kept = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(handBears, handThopter, kept)));
        List<Card> library = setLibrary(new Ornithopter(), new GrizzlyBears(), new Ornithopter());

        activateScrollRack();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(handBears.getId(), handThopter.getId()));

        // Two set aside, two moved off the library top, then the "in any order" prompt.
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(kept, library.get(0), library.get(1));
        var reorder = (PendingInteraction.LibraryReorder) gd.interaction.activeInteraction();
        assertThat(reorder.cards()).containsExactly(handBears, handThopter);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(handThopter, handBears, library.get(2));
    }

    @Test
    @DisplayName("Choosing a single card skips the ordering prompt and puts it straight on top")
    void singleCardGoesStraightOnTop() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handBears = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(handBears)));
        List<Card> library = setLibrary(new Ornithopter(), new GrizzlyBears());

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of(handBears.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handBears, library.get(1));
    }

    @Test
    @DisplayName("Choosing no cards leaves hand and library untouched")
    void choosingNothingDoesNothing() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handBears = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(handBears)));
        List<Card> library = setLibrary(new Ornithopter(), new GrizzlyBears());

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handBears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("A library shorter than the number set aside moves only what is there, and all set-aside cards come back")
    void shortLibraryMovesFewerCards() {
        harness.addToBattlefield(player1, new ScrollRack());
        Card handBears = new GrizzlyBears();
        Card handThopter = new Ornithopter();
        harness.setHand(player1, new ArrayList<>(List.of(handBears, handThopter)));
        List<Card> library = setLibrary(new Ornithopter());

        activateScrollRack();
        harness.handleMultipleCardsChosen(player1, List.of(handBears.getId(), handThopter.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handBears, handThopter);
    }

    @Test
    @DisplayName("An empty hand resolves with no prompt")
    void emptyHandResolvesSilently() {
        harness.addToBattlefield(player1, new ScrollRack());
        harness.setHand(player1, new ArrayList<>());
        List<Card> library = setLibrary(new Ornithopter(), new GrizzlyBears());

        activateScrollRack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }
}
