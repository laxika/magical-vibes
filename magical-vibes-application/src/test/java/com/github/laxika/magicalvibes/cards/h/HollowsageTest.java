package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HollowsageTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Hollowsage lets its controller make a target opponent discard")
    void untapMakesTargetOpponentDiscard() {
        Permanent hollowsage = addHollowsageTapped(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        runUntapStep(player1);
        assertThat(hollowsage.isTapped()).isFalse();

        // Trigger resolves into the "may" prompt.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        // Target opponent chooses the card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the trigger makes no one discard")
    void decliningDoesNothing() {
        addHollowsageTapped(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        runUntapStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller may target themselves with the discard")
    void mayTargetSelf() {
        addHollowsageTapped(player1);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        runUntapStep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent addHollowsageTapped(Player player) {
        Permanent perm = new Permanent(new Hollowsage());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /**
     * Advances from the opponent's turn into the given player's untap step so the engine actually
     * runs the untap (which is what fires the "becomes untapped" trigger).
     */
    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn: untaps and enqueues the trigger
    }
}
