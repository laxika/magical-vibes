package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DwellOnThePast.class)
class DwellOnThePastTest extends BaseCardTest {

    private void castDwell(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DwellOnThePast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Shuffles the chosen cards from the target player's graveyard into their library")
    void shufflesChosenCardsIntoLibrary() {
        harness.setGraveyard(player2, List.of(new DwellOnThePast(), new DwellOnThePast()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castDwell(player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }

    @Test
    @DisplayName("Caps the selection at four cards")
    void capsSelectionAtFourCards() {
        harness.setGraveyard(player1, List.of(
                new DwellOnThePast(), new DwellOnThePast(), new DwellOnThePast(),
                new DwellOnThePast(), new DwellOnThePast()));

        castDwell(player1.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Moves only the four selected cards when five are available")
    void movesOnlyFourSelectedCards() {
        List<Card> graveyardCards = List.of(
                new DwellOnThePast(), new DwellOnThePast(), new DwellOnThePast(),
                new DwellOnThePast(), new DwellOnThePast());
        harness.setGraveyard(player2, graveyardCards);
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castDwell(player2.getId());

        harness.handleMultipleCardsChosen(player1, graveyardCards.stream()
                .limit(4)
                .map(Card::getId)
                .toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCards.getLast());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 4);
    }

    @Test
    @DisplayName("Choosing zero cards leaves the graveyard unchanged")
    void choosingZeroCardsLeavesGraveyardUnchanged() {
        Card card = new DwellOnThePast();
        harness.setGraveyard(player1, List.of(card));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        castDwell(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
    }

    @Test
    @DisplayName("An empty graveyard needs no card choice and still causes a shuffle")
    void emptyGraveyardStillShufflesLibrary() {
        harness.setGraveyard(player2, List.of());
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn.put(player2.getId(), UUID.randomUUID());

        castDwell(player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn).doesNotContainKey(player2.getId());
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }

    @Test
    @DisplayName("Only selected cards still in the graveyard are shuffled into the library")
    void resolvesWithOneGraveyardTargetGone() {
        Card remaining = new DwellOnThePast();
        Card removed = new DwellOnThePast();
        harness.setGraveyard(player2, List.of(remaining, removed));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castDwell(player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySizeBefore + 1).contains(remaining).doesNotContain(removed);
        assertThat(gd.playerHands.get(player2.getId())).contains(removed);
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }

    @Test
    @DisplayName("The legal player target allows resolution and shuffling when every card target has left")
    void allGraveyardTargetsGoneStillShufflesLibrary() {
        Card removed = new DwellOnThePast();
        harness.setGraveyard(player2, List.of(removed));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn.put(player2.getId(), UUID.randomUUID());

        castDwell(player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore).doesNotContain(removed);
        assertThat(gd.playerHands.get(player2.getId())).contains(removed);
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn).doesNotContainKey(player2.getId());
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }

    @Test
    @CardUsed(GroundSeal.class)
    @DisplayName("Ground Seal entering before resolution makes the graveyard targets illegal but does not prevent shuffling")
    void graveyardTargetsBecomingUntargetableAreNotMoved() {
        Card target = new DwellOnThePast();
        harness.setGraveyard(player2, List.of(target));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn.put(player2.getId(), UUID.randomUUID());

        castDwell(player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.addToBattlefield(player2, new GroundSeal());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore).doesNotContain(target);
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn).doesNotContainKey(player2.getId());
        harness.assertInGraveyard(player1, "Dwell on the Past");
    }
}
