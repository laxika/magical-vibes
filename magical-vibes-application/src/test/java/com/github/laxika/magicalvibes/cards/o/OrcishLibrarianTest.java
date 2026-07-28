package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@DisplayName("Orcish Librarian")
class OrcishLibrarianTest extends BaseCardTest {

    private void addLibrarianReady() {
        harness.addToBattlefieldAndReturn(player1, new OrcishLibrarian()).setSummoningSick(false);
    }

    private List<Card> eightCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cards.add(new GrizzlyBears());
            cards.add(new Plains());
        }
        return cards;
    }

    @Test
    @DisplayName("Exiles four of the top eight at random and offers the rest for reordering")
    void exilesFourAtRandomAndReordersRest() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        List<Card> library = eightCards();
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).hasSize(4);
        assertThat(reorder.toBottom()).isFalse();
        assertThat(reorder.deckOwnerId()).isEqualTo(player1.getId());

        // The eight looked-at cards are split exactly between exile and the reorder pool.
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Answering the reorder puts the surviving cards back on top in the chosen order")
    void reorderPutsSurvivorsOnTop() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        List<Card> library = eightCards();
        library.add(new LlanowarElves()); // ninth card stays untouched below the top eight
        harness.setLibrary(player1, library);
        Card ninth = library.get(8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        Card wantedOnTop = reorder.cards().get(3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 0, 1, 2)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(wantedOnTop);
        assertThat(deck.get(4)).isSameAs(ninth);
    }

    @Test
    @DisplayName("With fewer cards than the exile count, every looked-at card is exiled")
    void smallLibraryExilesEverything() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Plains(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Five cards left after the random exiles means a single card goes straight back on top")
    void singleSurvivorGoesBackOnTopWithoutPrompt() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        Card survivorPool0 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(survivorPool0, new Plains(), new Plains(), new Plains(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
