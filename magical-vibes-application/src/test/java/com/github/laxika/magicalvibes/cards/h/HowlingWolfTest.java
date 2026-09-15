package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlingWolf.class, Forest.class})
class HowlingWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Howling Wolf creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Howling Wolf");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithWolves(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Accepting the may ability searches only for Howling Wolves")
    void acceptingMayInitiatesNamedLibrarySearch() {
        setupAndCast();
        setupLibraryWithWolves(2);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Howling Wolf"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();
    }

    @Test
    @DisplayName("Choosing three Howling Wolves puts them into hand")
    void choosingMultipleWolvesPutsThemIntoHand() {
        setupAndCast();
        setupLibraryWithWolves(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Howling Wolf")))
                .hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search cannot choose more than three Howling Wolves")
    void searchIsCappedAtThreeCards() {
        setupAndCast();
        setupLibraryWithWolves(4);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Howling Wolf")))
                .hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a card before reaching three cards")
    void canFailToFindEarly() {
        setupAndCast();
        setupLibraryWithWolves(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No Howling Wolves in the library produces no search prompt")
    void noWolvesInLibraryProducesNoSearchPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no cards named Howling Wolf"));
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new HowlingWolf(), "{2}{G}{G}");
    }

    private void setupLibraryWithWolves(int wolfCount) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < wolfCount; i++) {
            library.add(new HowlingWolf());
        }
        library.add(new Forest());
        library.add(new Forest());
        harness.setLibrary(player1, library);
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
