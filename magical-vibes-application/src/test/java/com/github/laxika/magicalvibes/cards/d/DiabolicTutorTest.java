package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicTutor.class, AvenFisher.class, Plains.class, Swamp.class})
class DiabolicTutorTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Diabolic Tutor puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Diabolic Tutor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Diabolic Tutor presents all cards from library for choice")
    void resolvingPresentsAllCardsForChoice() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // All cards from library are presented (not just a subset)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp", "Aven Fisher", "Aven Fisher");
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and shuffles library")
    void choosingCardPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));

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

        // Find Aven Fisher in the search cards
        int fisherIndex = -1;
        for (int i = 0; i < gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().size(); i++) {
            if (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().get(i).getName().equals("Aven Fisher")) {
                fisherIndex = i;
                break;
            }
        }
        assertThat(fisherIndex).isGreaterThanOrEqualTo(0);

        harness.handleCardChosen(player1, fisherIndex);

        harness.assertInHand(player1, "Aven Fisher");
    }

    @Test
    @DisplayName("Diabolic Tutor does not reveal the chosen card (unrestricted search)")
    void doesNotRevealChosenCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isFalse();

        harness.handleCardChosen(player1, 0);

        // Log should NOT mention "reveals"
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("reveals") && entry.contains("puts it into their hand"));
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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Player cannot fail to find with unrestricted search")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        setupAndCast();

        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("it is empty")).isTrue();
    }

    // ===== Completing the search fully finishes the paused resolution =====

    @Test
    @DisplayName("Completing the search leaves no dangling paused resolution")
    void completingSearchClearsPausedResolution() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

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

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new AvenFisher(), new AvenFisher()));
    }
}

