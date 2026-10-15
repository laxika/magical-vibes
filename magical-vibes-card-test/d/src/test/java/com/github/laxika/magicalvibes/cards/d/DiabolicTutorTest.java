package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicTutor.class, AvenFisher.class, CavesOfKoilos.class})
class DiabolicTutorTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Diabolic Tutor puts it on the stack")
    void castingPutsItOnStack() {
        DiabolicTutor tutor = new DiabolicTutor();
        harness.castFromHand(player1, tutor, "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(tutor);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Diabolic Tutor presents all cards from library for choice")
    void resolvingPresentsAllCardsForChoice() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // All cards from library are presented (not just a subset)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Caves of Koilos", "Caves of Koilos", "Aven Fisher", "Aven Fisher");
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and shuffles library")
    void choosingCardPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst();

        harness.handleCardChosen(player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);

        // Library lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Awaiting state is cleared
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Can choose a non-land card from library")
    void canChooseNonLandCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Aven Fisher is a non-land card in the search choices.
        Card fisher = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .filter(card -> card instanceof AvenFisher).findFirst().orElseThrow();
        int fisherIndex = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().indexOf(fisher);

        harness.handleCardChosen(player1, fisherIndex);

        assertThat(gd.playerHands.get(player1.getId())).contains(fisher);
    }

    @Test
    @DisplayName("Diabolic Tutor searches its controller's library")
    void searchesControllerLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new AvenFisher()));
        harness.setLibrary(player2, List.of(new CavesOfKoilos()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Aven Fisher");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Diabolic Tutor does not reveal the chosen card (unrestricted search)")
    void doesNotRevealChosenCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isFalse();

        harness.handleCardChosen(player1, 0);

        // Log should NOT mention "reveals"
        assertThat(gameLogContains("reveals")).isFalse();
        // Log should mention putting a card into hand
        assertThat(gameLogContains("puts a card into their hand")).isTrue();
    }

    // ===== Cannot fail to find (unrestricted search) =====

    @Test
    @DisplayName("Unrestricted search sets canFailToFind to false")
    void canFailToFindIsFalse() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Player cannot fail to find with unrestricted search")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        setupAndCast();

        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("it is empty. Library is shuffled.")).isTrue();
    }

    // ===== Completing the search fully finishes the paused resolution =====

    @Test
    @DisplayName("Completing the search leaves no dangling paused resolution")
    void completingSearchClearsPausedResolution() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        // The search was the spell's last effect: the parked resolution entry must be cleared
        // and the deferred player-loss check released, or the game state dangles mid-resolution.
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    // ===== Sorcery goes to graveyard after resolution =====

    @Test
    @DisplayName("Diabolic Tutor goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Diabolic Tutor");
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");
    }

    @Test
    @DisplayName("Searching a one-card library puts its last card into hand and finishes resolution")
    void searchesLastCardInLibrary() {
        AvenFisher lastCard = new AvenFisher();
        harness.setLibrary(player1, List.of(lastCard));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
        harness.assertInGraveyard(player1, "Diabolic Tutor");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An opponent cannot choose the card for Diabolic Tutor")
    void opponentCannotChooseCard() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();
        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new CavesOfKoilos(), new AvenFisher(), new CavesOfKoilos(), new AvenFisher()));
    }
}
