package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormfangManta.class})
class WormfangMantaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller skip their next turn")
    void enteringTheBattlefieldSkipsNextTurn() {
        castManta();

        assertThat(gd.skipNextTurnCount).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("Leaving the battlefield gives its controller an extra turn")
    void leavingTheBattlefieldGivesExtraTurn() {
        Permanent manta = castManta();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manta));
        resolveAllTriggers();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("If it leaves before its enters trigger resolves, the leave trigger resolves first")
    void leavingBeforeEnterTriggerResolvesPreservesBothTriggers() {
        harness.castFromHand(player1, new WormfangManta(), "{5}{U}{U}");
        harness.passBothPriorities();

        Permanent manta = findPermanent(player1, "Wormfang Manta");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manta));

        harness.passBothPriorities();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();
        assertThat(gd.skipNextTurnCount).containsEntry(player1.getId(), 1);
    }

    private Permanent castManta() {
        harness.castFromHand(player1, new WormfangManta(), "{5}{U}{U}");
        resolveAllTriggers();
        return findPermanent(player1, "Wormfang Manta");
    }

    @Test
    @DisplayName("Its controller's next turn is skipped")
    void skipsNextTurn() {
        castManta();

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The pending turn skip consumes the extra turn before the next normal turn")
    void pendingSkipConsumesExtraTurn() {
        Permanent manta = castManta();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manta));
        resolveAllTriggers();

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.skipNextTurnCount).doesNotContainKey(player1.getId());

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isFalse();
    }

    @Test
    @DisplayName("A Manta that leaves under the opponent's control gives the opponent the extra turn")
    void leavingUsesControllerRatherThanOwner() {
        Permanent manta = harness.addToBattlefieldAndReturn(player1, new WormfangManta());
        gd.playerBattlefields.get(player1.getId()).remove(manta);
        gd.playerBattlefields.get(player2.getId()).add(manta);
        gd.stolenCreatures.put(manta.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manta));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wormfang Manta");
        assertThat(gd.extraTurns).containsExactly(player2.getId());
        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("Returning Manta to hand also grants an extra turn")
    void returningToHandGivesExtraTurn() {
        Permanent manta = harness.addToBattlefieldAndReturn(player1, new WormfangManta());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, manta));
        resolveAllTriggers();

        harness.assertInHand(player1, "Wormfang Manta");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isFalse();
    }

    @Test
    @DisplayName("Manta entering under the opponent's control skips the opponent's turn")
    void enteringUnderOpponentControlSkipsOpponentTurn() {
        harness.enterBattlefieldAndReturn(player2, new WormfangManta());
        resolveAllTriggers();

        assertThat(gd.skipNextTurnCount).containsEntry(player2.getId(), 1);
        assertThat(gd.skipNextTurnCount).doesNotContainKey(player1.getId());
        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.skipNextTurnCount).doesNotContainKey(player2.getId());
    }
    private void advanceTurnForJudReview() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
