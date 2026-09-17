package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectiveVoyage.class, Forest.class, GrizzlyBears.class, Plains.class})
class CollectiveVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("The total mana paid lets each player search for that many tapped basic lands")
    void sharedManaTotalSetsEachSearchCount() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        harness.setLibrary(player1, List.of(firstForest, secondForest, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(firstPlains, secondPlains, new GrizzlyBears()));
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.forceActivePlayer(player2);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 1);

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(firstPlains, secondPlains);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().cards()).containsExactly(firstForest, secondForest);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Players can pay zero and choose no lands")
    void zeroPaymentsFindNoLands() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
