package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CrookclawTransmuter;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
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

@CardUsed({MysticalTeachings.class, Cancel.class, CrookclawTransmuter.class, FledglingMawcor.class})
class MysticalTeachingsTest extends BaseCardTest {

    @Test
    @DisplayName("Search offers instant cards and cards with flash")
    void searchOffersInstantsAndFlashCards() {
        Card instant = new Cancel();
        Card flashCreature = new CrookclawTransmuter();
        Card creature = new FledglingMawcor();
        castWithLibrary(List.of(instant, flashCreature, creature));

        GameData gd = harness.getGameData();
        harness.passBothPriorities();

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(instant, flashCreature);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and finishes the search")
    void choosingMatchingCardPutsItIntoHand() {
        Card instant = new Cancel();
        Card flashCreature = new CrookclawTransmuter();
        castWithLibrary(List.of(instant, flashCreature));

        GameData gd = harness.getGameData();
        harness.passBothPriorities();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(flashCreature));

        assertThat(gd.playerHands.get(player1.getId())).contains(flashCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a matching card shuffles the remaining library")
    void choosingMatchingCardShufflesLibrary() {
        Card instant = new Cancel();
        Card flashCreature = new CrookclawTransmuter();
        castWithLibrary(List.of(instant, flashCreature));

        harness.passBothPriorities();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(flashCreature));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("No matching cards end the search without a choice")
    void noMatchingCardsDoNotPrompt() {
        Card creature = new FledglingMawcor();
        castWithLibrary(List.of(creature));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Flashback searches and exiles Mystical Teachings after resolution")
    void flashbackSearchesAndExiles() {
        MysticalTeachings teachings = new MysticalTeachings();
        Card instant = new Cancel();
        harness.setGraveyard(player1, List.of(teachings));
        harness.setLibrary(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gameData = harness.getGameData();
        var search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gameData.getPlayerExiledCards(player1.getId())).contains(teachings);
        assertThat(gameData.playerGraveyards.get(player1.getId())).doesNotContain(teachings);
    }

    @Test
    @DisplayName("An instant without flash can be revealed and put into hand")
    void choosingInstantWithoutFlash() {
        Card instant = new Cancel();
        Card creature = new FledglingMawcor();
        castWithLibrary(List.of(instant, creature));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gameLogContains("reveals Cancel")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(MysticalTeachings.class::isInstance);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may find nothing even when matching cards are present")
    void mayDeclineMatchingCardsAndStillShuffle() {
        Card instant = new Cancel();
        Card flashCreature = new CrookclawTransmuter();
        castWithLibrary(List.of(instant, flashCreature));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, flashCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(instant, flashCreature);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows flashback to resolve and exile the spell")
    void flashbackWithEmptyLibraryExilesSpell() {
        MysticalTeachings teachings = new MysticalTeachings();
        harness.setGraveyard(player1, List.of(teachings));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(teachings);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(teachings);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new MysticalTeachings(), "{3}{U}");
    }
}
