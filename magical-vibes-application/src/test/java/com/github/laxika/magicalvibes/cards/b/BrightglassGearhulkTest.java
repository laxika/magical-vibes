package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightglassGearhulk.class, Memnite.class, LlanowarElves.class, WildGrowth.class,
        GrizzlyBears.class, GiantGrowth.class})
class BrightglassGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB search only shows qualifying artifact, creature, and enchantment cards")
    void searchOnlyShowsQualifyingCards() {
        setupAndCast();
        Memnite memnite = new Memnite();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth wildGrowth = new WildGrowth();
        setLibrary(memnite, elves, wildGrowth, new GrizzlyBears());

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyInAnyOrder(memnite, elves, wildGrowth);
    }

    @Test
    @DisplayName("The ETB search puts at most two selected cards into hand")
    void searchPutsAtMostTwoCardsIntoHand() {
        setupAndCast();
        Memnite memnite = new Memnite();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth wildGrowth = new WildGrowth();
        setLibrary(memnite, elves, wildGrowth);

        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).contains(wildGrowth);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library and hand unchanged")
    void decliningSearchDoesNothing() {
        setupAndCast();
        List<Card> library = List.of(new Memnite(), new GrizzlyBears());
        setLibrary(library.toArray(Card[]::new));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveMayAbility(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void mayChooseZeroCardsEvenWhenMatchesExist() {
        setupAndCast();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth growth = new WildGrowth();
        setLibrary(elves, growth);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elves, growth);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void mayStopAfterOneCard() {
        setupAndCast();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth growth = new WildGrowth();
        setLibrary(elves, growth);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(growth);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void mayFindTwoCardsWithTheSameNameAndType() {
        setupAndCast();
        LlanowarElves first = new LlanowarElves();
        LlanowarElves second = new LlanowarElves();
        setLibrary(first, second);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void cheapInstantIsExcludedFromBothSelections() {
        setupAndCast();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth growth = new WildGrowth();
        GiantGrowth instant = new GiantGrowth();
        setLibrary(elves, instant, growth);
        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(elves, growth);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(growth);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(elves, growth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void revealsTheOnlyQualifyingCardBeforeFinishingSearch() {
        setupAndCast();
        LlanowarElves elves = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(elves, bears);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("reveals Llanowar Elves")).isTrue();
    }

    @Test
    void searchWithNoQualifyingCardsFinishesWithoutAddingCardsToHand() {
        setupAndCast();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears);
        resolveMayAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new BrightglassGearhulk()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
