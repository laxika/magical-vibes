package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattalionFootSoldier.class, YokedOx.class})
class BattalionFootSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Battalion Foot Soldier creates a may prompt")
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
        setupLibraryWithSoldiers(3);

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3)
                .allMatch(card -> card instanceof BattalionFootSoldier);
    }

    @Test
    @DisplayName("Selecting all matching cards puts them into hand")
    void selectingAllMatchingCardsPutsThemIntoHand() {
        setupAndCast();
        setupLibraryWithSoldiers(3);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card ->
                card instanceof BattalionFootSoldier).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search may find no cards")
    void searchMayFindNoCards() {
        setupAndCast();
        setupLibraryWithSoldiers(2);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the ability leaves the library and hand unchanged")
    void decliningDoesNotSearch() {
        setupAndCast();
        setupLibraryWithSoldiers(2);
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player1.getId()));

        resolveMayAbility(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller may take one copy and leave other matching copies in the library")
    void mayStopAfterOneCopy() {
        setupAndCast();
        setupLibraryWithSoldiers(3);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof BattalionFootSoldier);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card instanceof BattalionFootSoldier).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card instanceof YokedOx).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An accepted search of an empty library completes without choosing a card")
    void emptyLibrarySearchCompletes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveMayAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An accepted search with no matching names leaves unrelated cards in the library")
    void noMatchingCardsSearchCompletes() {
        setupAndCast();
        YokedOx ox = new YokedOx();
        harness.setLibrary(player1, List.of(ox));

        resolveMayAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ox);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new BattalionFootSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setupLibraryWithSoldiers(int soldierCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < soldierCount; i++) {
            deck.add(new BattalionFootSoldier());
        }
        deck.add(new YokedOx());
        harness.setLibrary(player1, deck);
    }
}
