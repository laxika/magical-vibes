package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.h.HarvesterDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentBodyguard.class, GiantWarthog.class, HarvesterDruid.class, MoralityShift.class})
class MoralityShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges the controller's graveyard and library, then shuffles the new library")
    void exchangesGraveyardAndLibrary() {
        Card libraryCard = new GiantWarthog();
        Card graveyardCard = new HarvesterDruid();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.castFromHand(player1, new MoralityShift(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
        harness.assertInGraveyard(player1, "Morality Shift");
    }

    @Test
    @DisplayName("Only exchanges the controller's zones")
    void opponentZonesAreUntouched() {
        Card opponentLibraryCard = new GiantWarthog();
        Card opponentGraveyardCard = new HarvesterDruid();
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.setGraveyard(player1, List.of(new HarvesterDruid()));
        harness.castFromHand(player1, new MoralityShift(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }

    @Test
    @DisplayName("Moves the library to the graveyard even when the graveyard is empty")
    void movesLibraryWhenGraveyardIsEmpty() {
        Card libraryCard = new GiantWarthog();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new MoralityShift(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(libraryCard);
        harness.assertInGraveyard(player1, "Morality Shift");
    }

    @Test
    @DisplayName("Handles an empty graveyard")
    void handlesEmptyGraveyard() {
        Card libraryCard = new BenevolentBodyguard();
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, moralityShift);
    }

    @Test
    @DisplayName("Moves the graveyard into an empty library without including the resolving spell")
    void exchangesWhenLibraryIsEmpty() {
        Card graveyardCard = new HarvesterDruid();
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(moralityShift);
    }

    @Test
    @DisplayName("Exchanges zones with different numbers of cards")
    void exchangesUnequalZoneSizes() {
        Card libraryCard = new GiantWarthog();
        Card firstGraveyardCard = new HarvesterDruid();
        Card secondGraveyardCard = new BenevolentBodyguard();
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(firstGraveyardCard, secondGraveyardCard));
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyardCard, secondGraveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(libraryCard, moralityShift);
    }

    @Test
    @DisplayName("Resolves when both exchanged zones are empty")
    void exchangesEmptyZones() {
        Card moralityShift = new MoralityShift();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, moralityShift, "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(moralityShift);
    }

    @Test
    @DisplayName("Lets the owner choose the order of multiple cards entering the graveyard")
    void offersGraveyardOrderingChoice() {
        harness.setLibrary(player1, List.of(new GiantWarthog(), new BenevolentBodyguard()));
        harness.setGraveyard(player1, List.of(new HarvesterDruid()));
        harness.castFromHand(player1, new MoralityShift(), "{5}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
