package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FabledPassage.class, Forest.class, Island.class, Plains.class, ColossalDreadmaw.class})
class FabledPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Fabled Passage and searches for a basic land onto the battlefield tapped")
    void activationSearchesForBasicLand() {
        addPassageWithLands(2);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fabled Passage");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(card -> card instanceof ColossalDreadmaw);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("The fetched land stays tapped when it is the third land")
    void fetchedLandStaysTappedBelowFourLands() {
        addPassageWithLands(2);
        setupLibrary();

        chooseFetchedLand();

        assertThat(findPermanents(player1, "Forest"))
                .singleElement()
                .matches(p -> p.isTapped());
    }

    @Test
    @DisplayName("The fetched land is untapped when it makes four lands")
    void fetchedLandUntapsAtFourLands() {
        addPassageWithLands(3);
        setupLibrary();

        chooseFetchedLand();

        assertThat(findPermanents(player1, "Forest"))
                .singleElement()
                .matches(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Only the searched land is untapped, not other tapped lands")
    void onlyFetchedLandUntaps() {
        addPassageWithLands(4);
        findPermanents(player1, "Island").forEach(p -> p.tap());
        setupLibrary();

        chooseFetchedLand();

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(findPermanents(player1, "Island")).allMatch(p -> p.isTapped());
    }

    @Test
    @DisplayName("Opponents' lands do not count toward the untap condition")
    void opponentsLandsDoNotCount() {
        addPassageWithLands(2);
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        setupLibrary();

        chooseFetchedLand();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A search may fail to find even when basic lands are available")
    void mayFailToFind() {
        addPassageWithLands(4);
        findPermanents(player1, "Island").forEach(p -> p.tap());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Fabled Passage");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(countPermanents(player1, "Forest")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(findPermanents(player1, "Island")).allMatch(p -> p.isTapped());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("A library without basic lands finishes the search without untapping anything")
    void noBasicLandsInLibrary() {
        addPassageWithLands(4);
        findPermanents(player1, "Island").forEach(p -> p.tap());
        harness.setLibrary(player1, List.of(new ColossalDreadmaw(), new FabledPassage()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fabled Passage");
        assertThat(countPermanents(player1, "Fabled Passage")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Island")).allMatch(p -> p.isTapped());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void chooseFetchedLand() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void addPassageWithLands(int landCount) {
        harness.addToBattlefield(player1, new FabledPassage());
        for (int i = 0; i < landCount; i++) {
            harness.addToBattlefield(player1, new Island());
        }
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new ColossalDreadmaw()));
    }
}
