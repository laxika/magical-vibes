package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PullFromTomorrowTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with the chosen X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PullFromTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3: {3}{U}{U} = 5

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    // ===== Resolution: draw X then discard =====

    @Test
    @DisplayName("X=3 draws three cards then prompts for a discard")
    void xThreeDrawsThreeThenPromptsForDiscard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PullFromTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        // Spell left hand, then drew 3
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        // Awaiting the discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.DiscardChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Completing the discard results in a net gain of two cards")
    void completingDiscardResultsInNetGainOfTwo() {
        harness.setHand(player1, List.of(new PullFromTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Drew 3, discarded 1 -> 2 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X=1 draws one card then discards it, netting zero cards")
    void xOneDrawsOneThenDiscards() {
        harness.setHand(player1, List.of(new PullFromTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1: {1}{U}{U} = 3

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Goes to graveyard and clears the stack after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new PullFromTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pull from Tomorrow"));
    }
}
