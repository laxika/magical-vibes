package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScholarshipSponsor.class, Forest.class, Island.class, CommandTower.class,
        ObNixilisUnshackled.class})
class ScholarshipSponsorTest extends BaseCardTest {

    @Test
    @DisplayName("Each player below the land leader searches for up to the difference in tapped basics")
    void searchesForDifferenceFromMostLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setLibrary(player1, List.of(firstForest, secondForest));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(3)
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Players tied for the most lands do not search")
    void tiedPlayersDoNotSearch() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player2, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("An opponent may take fewer basics than their land deficit")
    void opponentMayStopAfterOneLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(findPermanents(player2, "Island")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taking zero lands still counts as the required library search")
    void zeroLandsStillTriggersOpponentSearchPenalty() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Island()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        assertThat(findPermanents(player2, "Island")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonbasic lands count toward the leader but cannot be found")
    void countsNonbasicLandsButOnlyFindsBasics() {
        harness.addToBattlefield(player2, new CommandTower());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new CommandTower(), new ScholarshipSponsor(), new Forest()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        harness.assertNotOnBattlefield(player1, "Command Tower");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Land counts are determined when the enters ability resolves")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new Island());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(2)
                .filteredOn(Permanent::isTapped).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An eligible player with no basics finishes the search without taking a card")
    void noBasicsFinishesSearch() {
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new ScholarshipSponsor(), new CommandTower()));
        harness.castFromHand(player1, new ScholarshipSponsor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Command Tower");
        assertThat(gd.stack).isEmpty();
    }
}
