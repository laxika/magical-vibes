package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquadronHawk.class, RuneclawBear.class})
class SquadronHawkTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving Squadron Hawk creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        harness.assertOnBattlefield(player1, "Squadron Hawk");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Accepting may ability initiates library search showing only Squadron Hawks")
    void acceptingMayInitiatesSearch() {
        setupAndCast();
        setupLibraryWithHawks(2);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Squadron Hawk"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Squadron Hawk from search puts it into hand")
    void choosingHawkPutsItIntoHand() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Squadron Hawk");
    }

    @Test
    @DisplayName("Can search for multiple hawks sequentially")
    void canSearchForMultipleHawks() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first hawk
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Pick second hawk
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Pick third hawk
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        long hawksInHand = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Squadron Hawk"))
                .count();
        assertThat(hawksInHand).isEqualTo(3);
    }

    @Test
    @DisplayName("Can fail to find early by choosing no card")
    void canFailToFindEarly() {
        setupAndCast();
        setupLibraryWithHawks(3);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first hawk
        harness.handleCardChosen(player1, 0);

        // Fail to find (pass on second pick)
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No hawks in library results in shuffled library and no search prompt")
    void noHawksInLibraryShuffles() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no cards named Squadron Hawk"));
    }

    @Test
    @DisplayName("Cards found are revealed")
    void cardsAreRevealed() {
        setupAndCast();
        setupLibraryWithHawks(1);

        harness.passBothPriorities(); // Resolve creature → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Searching can find zero Hawks even when matches exist")
    void canChooseZeroHawks() {
        setupAndCast();
        setupLibraryWithHawks(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Search ends after three Hawks even if more remain")
    void cannotFindMoreThanThreeHawks() {
        setupAndCast();
        setupLibraryWithHawks(4);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card instanceof SquadronHawk)).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(entry -> entry.contains("reveals Squadron Hawk"))).hasSize(3);
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes the ability")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SquadronHawk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithHawks(int hawkCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < hawkCount; i++) {
            deck.add(new SquadronHawk());
        }
        deck.add(new RuneclawBear());
        deck.add(new RuneclawBear());
        harness.setLibrary(player1, deck);
    }
}
