package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectiveVoyage.class, Forest.class, Plains.class, GrizzlyBears.class})
class CollectiveVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("Players may contribute mana and each may fetch up to the shared total")
    void playersContributeToSharedTotal() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains(), new GrizzlyBears()));
        castCollectiveVoyage(4, 2);

        assertThat(activeManaChoice().playerId()).isEqualTo(player1.getId());
        assertThat(activeManaChoice().maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        assertThat(activeManaChoice().playerId()).isEqualTo(player2.getId());
        assertThat(activeManaChoice().maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player2, 1);
        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        chooseLand(player1);
        chooseLand(player1);
        chooseNoLand(player1);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(activeSearch().params().remainingCount()).isEqualTo(3);

        chooseLand(player2);
        chooseNoLand(player2);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(lands(player1)).allMatch(permanent -> permanent.isTapped());
        assertThat(lands(player2)).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying zero still searches and shuffles each library")
    void payingZeroStillSearchesAndShuffles() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castCollectiveVoyage(2, 1);

        harness.handleXValueChosen(player1, 0);

        assertThat(activeManaChoice().playerId()).isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A player may find zero lands after the shared amount is positive")
    void eachPlayerMayFindZeroLands() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castCollectiveVoyage(2, 1);

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 0);

        chooseNoLand(player1);
        chooseNoLand(player2);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
    }

    private void castCollectiveVoyage(int player1Mana, int player2Mana) {
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, player1Mana);
        harness.addMana(player2, ManaColor.GREEN, player2Mana);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.XValueChoice activeManaChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseLand(Player player) {
        gs.handleInteractionAnswer(gd, player, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void chooseNoLand(Player player) {
        gs.handleInteractionAnswer(gd, player, new InteractionAnswer.LibraryCardChosen(-1));
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }

    private List<Permanent> lands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .toList();
    }
}
