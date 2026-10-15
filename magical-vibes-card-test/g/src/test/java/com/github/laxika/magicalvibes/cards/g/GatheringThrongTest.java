package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaffinesInformant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatheringThrong.class, RaffinesInformant.class})
class GatheringThrongTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gathering Throng creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability searches for every matching card in the library")
    void acceptingSearchesForMatchingCards() {
        setupAndCast();
        setupLibraryWithThrongs(3);

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3)
                .allMatch(card -> card instanceof GatheringThrong);
    }

    @Test
    @DisplayName("Selecting all matching cards puts them into hand")
    void selectingAllMatchingCardsPutsThemIntoHand() {
        setupAndCast();
        setupLibraryWithThrongs(3);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card ->
                card instanceof GatheringThrong).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search may find no cards")
    void searchMayFindNoCards() {
        setupAndCast();
        setupLibraryWithThrongs(2);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search can stop after selecting only some matching cards")
    void searchCanStopAfterOneCard() {
        setupAndCast();
        setupLibraryWithThrongs(3);
        resolveMayAbility(true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .filteredOn(card -> card instanceof GatheringThrong).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining the ability leaves the library untouched")
    void decliningDoesNotSearchOrShuffle() {
        setupAndCast();
        setupLibraryWithThrongs(2);
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player1.getId()));

        resolveMayAbility(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("searches their library")).isFalse();
        assertThat(gameLogContains("shuffled")).isFalse();
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes the ability")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveMayAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Every selected copy is revealed, including the last matching copy")
    void revealsEverySelectedCopy() {
        setupAndCast();
        setupLibraryWithThrongs(3);
        resolveMayAbility(true);

        for (int i = 0; i < 3; i++) {
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.gameLog.stream().filter(entry ->
                entry.plainText().contains(" reveals Gathering Throng"))).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .isInstanceOf(RaffinesInformant.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching a library with no matching cards leaves the hand unchanged")
    void noMatchingCardsCompletesSearch() {
        setupAndCast();
        setupLibraryWithThrongs(0);

        resolveMayAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .isInstanceOf(RaffinesInformant.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GatheringThrong()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setupLibraryWithThrongs(int throngCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < throngCount; i++) {
            deck.add(new GatheringThrong());
        }
        deck.add(new RaffinesInformant());
        harness.setLibrary(player1, deck);
    }
}
