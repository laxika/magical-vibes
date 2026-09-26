package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HondenOfNightsReach.class, HondenOfSeeingWinds.class})
class HondenOfNightsReachTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Target opponent discards one card with a single Shrine")
    void discardsOneCardWithOneShrine() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.setHand(player2, List.of(
                new HondenOfNightsReach(), new HondenOfSeeingWinds(), new HondenOfNightsReach()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Honden of Night's Reach");
    }

    @Test
    @DisplayName("Discard count scales with the number of Shrines the controller controls")
    void discardCountScalesWithShrines() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setHand(player2, List.of(
                new HondenOfNightsReach(), new HondenOfSeeingWinds(), new HondenOfNightsReach()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        // Honden of Seeing Winds' own upkeep draw trigger may resolve first; drain both triggers.
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName())
                .isEqualTo("Honden of Night's Reach");
    }

    @Test
    @DisplayName("Counts Shrines when the discard trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.setHand(player2, List.of(
                new HondenOfNightsReach(), new HondenOfSeeingWinds(), new HondenOfNightsReach()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Opponent with an empty hand is not prompted")
    void emptyHandNoPrompt() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Shrines the opponent controls do not increase the discard count")
    void opponentShrinesDoNotCount() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.addToBattlefield(player2, new HondenOfSeeingWinds());
        harness.setHand(player2, List.of(
                new HondenOfNightsReach(), new HondenOfSeeingWinds(), new HondenOfNightsReach()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
    }
}
