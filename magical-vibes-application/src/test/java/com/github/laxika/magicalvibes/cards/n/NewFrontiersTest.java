package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CabalPit;
import com.github.laxika.magicalvibes.cards.c.CephalidColiseum;
import com.github.laxika.magicalvibes.cards.f.FieldOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Simplify;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewFrontiers.class, CabalPit.class, CephalidColiseum.class, FieldOfTheDead.class,
        Forest.class, Island.class, ObNixilisUnshackled.class, Plains.class, Simplify.class,
        Swamp.class})
class NewFrontiersTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may search for up to X basic lands, which enter tapped in APNAP order")
    void eachPlayerSearchesForUpToXBasicLandsTapped() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        harness.setLibrary(player1, List.of(firstForest, secondForest, new Simplify()));
        harness.setLibrary(player2, List.of(firstPlains, secondPlains, new Simplify()));
        castNewFrontiers(2);

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(firstForest, secondForest);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(firstPlains, secondPlains);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gameLogContains(player1.getUsername() + "'s library is shuffled.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + "'s library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Lands chosen by one search enter simultaneously")
    void chosenLandsEnterSimultaneously() {
        harness.addToBattlefield(player1, new FieldOfTheDead());
        harness.addToBattlefield(player1, new CabalPit());
        harness.addToBattlefield(player1, new CephalidColiseum());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.setLibrary(player2, List.of());
        castNewFrontiers(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("Each player may decline the search")
    void eachPlayerMayDecline() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(1);

        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each player may choose fewer than X basic lands")
    void eachPlayerMayChooseFewerThanX() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Simplify()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Simplify()));
        castNewFrontiers(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
    }

    @Test
    @DisplayName("A player without a basic land is skipped")
    void playerWithoutBasicLandIsSkipped() {
        harness.setLibrary(player1, List.of(new Simplify()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(1);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
    }

    @Test
    @DisplayName("An empty library still counts as a search")
    void emptyLibraryStillTriggersOpponentSearchAbility() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        castNewFrontiers(1);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("With X=0 New Frontiers does not start a search")
    void xZeroDoesNothing() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castNewFrontiers(int xValue) {
        harness.setHand(player1, List.of(new NewFrontiers()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
