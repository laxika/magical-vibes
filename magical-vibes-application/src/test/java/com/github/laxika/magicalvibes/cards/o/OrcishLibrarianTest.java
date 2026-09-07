package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Orcish Librarian")
@CardUsed({OrcishLibrarian.class, BalduvianBears.class, Island.class, Plains.class})
class OrcishLibrarianTest extends BaseCardTest {

    private Permanent addLibrarianReady() {
        return addCreatureReady(player1, new OrcishLibrarian());
    }

    private List<Card> eightCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cards.add(new BalduvianBears());
            cards.add(new Plains());
        }
        return cards;
    }

    @Test
    @DisplayName("Pays red mana and taps when activated")
    void paysManaAndTapsWhenActivated() {
        Permanent librarian = addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new BalduvianBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(librarian.isTapped()).isTrue();
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

        List<Card> lookedAt = new ArrayList<>(gd.getPlayerExiledCards(player1.getId()));
        lookedAt.addAll(reorder.cards());
        assertThat(lookedAt).containsExactlyInAnyOrderElementsOf(library);

        // The eight looked-at cards are split exactly between exile and the reorder pool.
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Answering the reorder puts the surviving cards back on top in the chosen order")
    void reorderPutsSurvivorsOnTop() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        List<Card> library = eightCards();
        library.add(new Island()); // ninth card stays untouched below the top eight
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
        assertThat(deck).containsExactly(
                wantedOnTop, reorder.cards().get(0), reorder.cards().get(1), reorder.cards().get(2), ninth);
    }

    @Test
    @DisplayName("With fewer cards than the exile count, every looked-at card is exiled")
    void smallLibraryExilesEverything() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new BalduvianBears(), new Plains(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library leaves no cards to exile or reorder")
    void emptyLibraryDoesNothing() {
        Permanent librarian = addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(librarian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Five cards left after the random exiles means a single card goes straight back on top")
    void singleSurvivorGoesBackOnTopWithoutPrompt() {
        addLibrarianReady();
        harness.addMana(player1, ManaColor.RED, 1);
        Card survivorPool0 = new BalduvianBears();
        harness.setLibrary(player1, List.of(survivorPool0, new Plains(), new Plains(), new Plains(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
