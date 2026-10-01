package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hollowsage.class, SafeholdSentry.class})
class HollowsageTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Hollowsage lets its controller make a target opponent discard")
    void untapMakesTargetOpponentDiscard() {
        Permanent hollowsage = addHollowsageTapped(player1);
        harness.setHand(player2, List.of(new SafeholdSentry()));

        runUntapStep(player1);
        assertThat(hollowsage.isTapped()).isFalse();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        // Target opponent chooses the card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("Declining the trigger makes no one discard")
    void decliningDoesNothing() {
        addHollowsageTapped(player1);
        harness.setHand(player2, List.of(new SafeholdSentry()));

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
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
        harness.setHand(player1, List.of(new SafeholdSentry()));

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Safehold Sentry");
    }

    @Test
    @DisplayName("An already untapped Hollowsage does not trigger")
    void alreadyUntappedDoesNotTrigger() {
        Permanent hollowsage = addCreatureReady(player1, new Hollowsage());
        harness.setHand(player2, List.of(new SafeholdSentry()));

        advanceToUpkeep(player1);

        assertThat(hollowsage.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addHollowsageTapped(Player player) {
        Permanent perm = addCreatureReady(player, new Hollowsage());
        perm.tap();
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
