package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.a.AkrasanSquire;
import com.github.laxika.magicalvibes.cards.w.WildNacatl;
import com.github.laxika.magicalvibes.cards.e.ExecutionersCapsule;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RangerOfEos.class, AkrasanSquire.class, WildNacatl.class, ElvishVisionary.class, ExecutionersCapsule.class})
class RangerOfEosTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the search shows only creatures with mana value 1 or less")
    void acceptingSearchShowsOnlyLowManaValueCreatures() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new AkrasanSquire(), new WildNacatl(), new ElvishVisionary()));

        acceptSearch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> shown = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(shown).hasSize(2);
        assertThat(shown).noneMatch(c -> c.getName().equals("Elvish Visionary"));
    }

    @Test
    @DisplayName("Choosing creatures puts up to two into hand")
    void choosingCreaturesPutsThemIntoHand() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new AkrasanSquire(), new WildNacatl(), new ElvishVisionary()));

        acceptSearch();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not search the library")
    void decliningSkipsSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new AkrasanSquire(), new WildNacatl()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RangerOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
    }

    @Test
    void canFindZeroEvenWhenEligibleCardsExist() {
        setupAndCast();
        Card squire = new AkrasanSquire();
        Card nacatl = new WildNacatl();
        harness.setLibrary(player1, List.of(squire, nacatl));
        acceptSearch();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(squire, nacatl);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    @Test
    void canFindTwoWithTheSameNameButCannotTakeAThird() {
        setupAndCast();
        Card first = new AkrasanSquire();
        Card second = new AkrasanSquire();
        Card third = new AkrasanSquire();
        harness.setLibrary(player1, List.of(first, second, third));
        acceptSearch();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("reveals Akrasan Squire"))).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    @Test
    void canStopAfterFindingOneCreature() {
        setupAndCast();
        Card squire = new AkrasanSquire();
        Card nacatl = new WildNacatl();
        harness.setLibrary(player1, List.of(squire, nacatl));
        acceptSearch();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(squire);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nacatl);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Akrasan Squire"));
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    @Test
    void onlyOneEligibleCreatureCompletesSearchAfterOnePick() {
        setupAndCast();
        Card squire = new AkrasanSquire();
        Card visionary = new ElvishVisionary();
        Card capsule = new ExecutionersCapsule();
        harness.setLibrary(player1, List.of(squire, visionary, capsule));
        acceptSearch();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(squire);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(visionary, capsule);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Akrasan Squire"));
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    @Test
    void noEligibleCreaturesStillSearchesAndShuffles() {
        setupAndCast();
        Card visionary = new ElvishVisionary();
        Card capsule = new ExecutionersCapsule();
        harness.setLibrary(player1, List.of(visionary, capsule));
        acceptSearch();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(visionary, capsule);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    @Test
    void emptyLibraryCompletesAcceptedSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        acceptSearch();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }

    private void acceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
