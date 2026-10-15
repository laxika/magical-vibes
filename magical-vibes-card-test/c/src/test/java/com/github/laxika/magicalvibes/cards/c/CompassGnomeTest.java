package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompassGnome.class, CavernousMaw.class, Forest.class})
class CompassGnomeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers basic lands and Caves and puts the choice on top")
    void acceptsBasicLandOrCaveSearch() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        Card nonmatching = new CompassGnome();
        castGnome(List.of(basicLand, cave, nonmatching));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(basicLand, cave);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isIn(basicLand, cave);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the search")
    void declinesSearch() {
        Card basicLand = new Forest();
        castGnome(List.of(basicLand));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsChosenCaveOnTopAfterShuffling() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        Card nonmatching = new CompassGnome();
        castGnome(List.of(basicLand, nonmatching, cave));
        acceptEtbSearch();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(cave);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(basicLand, cave, nonmatching);
        assertThat(gameLogContains("reveals Cavernous Maw")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWithMatchingCards() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        castGnome(List.of(basicLand, cave));
        acceptEtbSearch();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(basicLand, cave);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithoutMatchingCardsFinishes() {
        Card nonmatching = new CompassGnome();
        castGnome(List.of(nonmatching));
        acceptEtbSearch();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchOfEmptyLibraryFinishes() {
        castGnome(List.of());
        acceptEtbSearch();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castGnome(List<Card> library) {
        harness.setHand(player1, List.of(new CompassGnome()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }

    private void acceptEtbSearch() {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
    }
}
