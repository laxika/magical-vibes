package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Foresight.class, FatalLore.class, ForceOfWill.class})
class ForesightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving searches the library and exiles the three chosen cards")
    void exilesThreeChosenCards() {
        List<Card> libraryCards = setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(libraryCards);
        assertThat(gd.exiledCards.stream()
                .filter(entry -> entry.ownerId().equals(player1.getId())))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Foresight");
    }

    @Test
    @DisplayName("Resolving schedules a draw at the beginning of the next turn's upkeep")
    void schedulesUpkeepDraw() {
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw happens at the next upkeep, even on the opponent's turn")
    void drawsAtNextUpkeep() {
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).add(new FatalLore());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("A library with fewer than three cards exiles what it can")
    void shortLibraryExilesWhatItCan() {
        harness.setHand(player1, List.of(new Foresight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card onlyCard = new FatalLore();
        deck.add(onlyCard);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.exiledCards.stream()
                .filter(entry -> entry.ownerId().equals(player1.getId())))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    private List<Card> setupAndCast() {
        harness.setHand(player1, List.of(new Foresight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        List<Card> libraryCards = List.of(new FatalLore(), new ForceOfWill(), new Foresight());
        deck.addAll(libraryCards);
        return libraryCards;
    }

    @Test
    @DisplayName("The search is unrestricted: cards are not revealed and it cannot fail to find")
    void unrestrictedSearch() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.EXILE);
        assertThat(search.params().shuffleAfterSelection()).isTrue();
    }

    @Test
    @CardUsed(PsychogenicProbe.class)
    @DisplayName("An empty library is still shuffled after the search")
    void emptyLibraryStillShuffles() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Foresight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.playerDecks.get(player1.getId()).clear();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
