package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldThrush.class, Forest.class, GrizzlyBears.class})
class OldThrushTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 2 life and offers a basic land search")
    void gainsLifeAndOffersBasicLandSearch() {
        Card basicLand = new Forest();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basicLand);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search still gains 2 life and leaves the library unchanged")
    void decliningSearchStillGainsLife() {
        Card basicLand = new Forest();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand, nonland);
    }

    @Test
    @DisplayName("A restricted search may fail to find even when a basic land is present")
    void mayFailToFindAnAvailableBasicLand() {
        Card basicLand = new Forest();
        Card nonland = new OldThrush();
        setup(List.of(nonland, basicLand));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonland, basicLand);
    }

    @Test
    @DisplayName("Searching an empty library still gains life and completes the ability")
    void searchingEmptyLibraryCompletesAbility() {
        setup(List.of());

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library with no basic lands leaves all cards in the library")
    void searchingWithoutBasicLandsCompletesAbility() {
        Card nonland = new OldThrush();
        setup(List.of(nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A basic land found below the top is revealed and ends up on top after shuffling")
    void foundLandIsRevealedAndPlacedOnTop() {
        Card firstNonland = new OldThrush();
        Card secondNonland = new OldThrush();
        Card basicLand = new Forest();
        setup(List.of(firstNonland, secondNonland, basicLand));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, secondNonland, basicLand);
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    private void setup(List<Card> library) {
        harness.setHand(player1, List.of(new OldThrush()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
