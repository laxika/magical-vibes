package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({CollectiveVoyage.class, Forest.class, Plains.class, LlanowarElves.class})
class CollectiveVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("The total mana paid lets each player search for that many tapped basic lands")
    void sharedManaTotalSetsEachSearchCount() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        harness.setLibrary(player1, List.of(firstForest, secondForest, new LlanowarElves()));
        harness.setLibrary(player2, List.of(firstPlains, secondPlains, new LlanowarElves()));
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
        harness.forceActivePlayer(player2);
        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("All players choose lands before any of the searched lands enter")
    void landsEnterTogetherAfterAllPlayersChoose() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(plains));
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(plains);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    @DisplayName("A player who pays nothing still benefits and may stop finding lands early")
    void zeroContributorCanFindFewerThanTheTotal() {
        Forest forest = new Forest();
        Forest unchosenForest = new Forest();
        harness.setLibrary(player1, List.of(forest, unchosenForest));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosenForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Having no basic lands does not stop the other player's search")
    void noBasicLandsDoesNotStopTheOtherSearch() {
        LlanowarElves elves = new LlanowarElves();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(elves));
        harness.setLibrary(player2, List.of(plains));
        harness.setHand(player1, List.of(new CollectiveVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 1);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(plains);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
