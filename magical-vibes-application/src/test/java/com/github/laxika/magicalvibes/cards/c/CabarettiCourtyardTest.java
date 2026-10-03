package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RunOutOfTown;
import com.github.laxika.magicalvibes.cards.s.SparasHeadquarters;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabarettiCourtyard.class, Forest.class, Island.class, Mountain.class, Plains.class,
        RunOutOfTown.class, SparasHeadquarters.class})
class CabarettiCourtyardTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Cabaretti Courtyard before the search trigger resolves")
    void enteringSacrificesIt() {
        CabarettiCourtyard courtyard = new CabarettiCourtyard();
        harness.setHand(player1, List.of(courtyard));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(courtyard.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(courtyard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(courtyard.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Mountain, Forest, or Plains and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playCourtyard();
        Card plains = new Plains();
        Card forest = new Forest();
        Card mountain = new Mountain();
        setLibrary(plains, forest, mountain, new RunOutOfTown());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, forest, mountain);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playCourtyard();
        Card plains = new Plains();
        setLibrary(plains, new Forest(), new Mountain());

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(plains.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playCourtyard();
        setLibrary(new RunOutOfTown());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Sacrifice and search resolve as separate triggers")
    void sacrificeQueuesSearchWithoutGainingLifeYet() {
        harness.setLife(player1, 20);
        playCourtyard();
        Card forest = new Forest();
        setLibrary(forest);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cabaretti Courtyard");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CabarettiCourtyard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Can fail to find even with an eligible land and still gains life")
    void failingToFindStillGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        playCourtyard();
        Card mountain = new Mountain();
        setLibrary(mountain);

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Empty library still allows the life gain")
    void emptyLibraryStillGainsLife() {
        harness.setLife(player1, 20);
        playCourtyard();
        setLibrary();

        resolveToSearchPrompt();

        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search excludes an Island and a nonbasic land with allowed land types")
    void excludesWrongBasicTypeAndNonbasicLand() {
        playCourtyard();
        Card forest = new Forest();
        setLibrary(new Island(), new SparasHeadquarters(), forest);

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .anyMatch(card -> card instanceof Island)
                .anyMatch(card -> card instanceof SparasHeadquarters);
        harness.assertOnBattlefield(player1, "Forest");
    }

    private void playCourtyard() {
        harness.setHand(player1, List.of(new CabarettiCourtyard()));
        harness.playLand(player1, 0);
    }

    private void resolveToSearchPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
