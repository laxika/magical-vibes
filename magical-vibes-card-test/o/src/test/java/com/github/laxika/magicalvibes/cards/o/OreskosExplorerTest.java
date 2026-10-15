package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OreskosExplorer.class, Forest.class, Plains.class})
class OreskosExplorerTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("ETB searches for up to one Plains for each opponent with more lands")
    void searchesForNumberOfPlainsMatchingOpponentsWithMoreLands() {
        addThirdPlayer();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player3, new Forest());
        harness.addToBattlefield(player3, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new OreskosExplorer()));

        castExplorer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).hasSize(3).allMatch(card -> card instanceof Plains);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Plains).hasSize(2);
    }

    @Test
    @DisplayName("ETB searches and shuffles without finding cards when X is zero")
    void searchesWithoutFindingWhenNoPlayerHasMoreLands() {
        harness.setLibrary(player1, List.of(new Plains(), new OreskosExplorer()));

        castExplorer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Plains).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Land counts are evaluated when the ETB trigger resolves, and ties do not count")
    void evaluatesLandCountsAtResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Forest()));

        castExplorer();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Player may choose zero Plains even when a matching card is available")
    void mayChooseZeroPlains() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        castExplorer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Player may stop after finding fewer Plains than X")
    void mayStopAfterOnePlains() {
        addThirdPlayer();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player3, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Forest()));

        castExplorer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).allMatch(card -> card instanceof Plains);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Plains"));
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Searching with no Plains available finishes and shuffles")
    void finishesWhenNoPlainsAreAvailable() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new OreskosExplorer()));

        castExplorer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
    }

    private void castExplorer() {
        harness.setHand(player1, List.of(new OreskosExplorer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
