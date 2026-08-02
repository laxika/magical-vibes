package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discard count scales with the number of Shrines the controller controls")
    void discardCountScalesWithShrines() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        // Honden of Seeing Winds' own upkeep draw trigger may resolve first; drain both triggers.
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Opponent with an empty hand is not prompted")
    void emptyHandNoPrompt() {
        harness.addToBattlefield(player1, new HondenOfNightsReach());
        harness.setHand(player2, new ArrayList<>(List.of()));

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
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
    }
}
