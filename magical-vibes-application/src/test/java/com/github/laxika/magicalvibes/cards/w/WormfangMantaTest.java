package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        castMantaForJudReview();

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurnForJudReview();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount).doesNotContainKey(player1.getId());
    }

    private Permanent castMantaForJudReview() {
        harness.setHand(player1, List.of(new WormfangManta()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Wormfang Manta");
    }

    private void advanceTurnForJudReview() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
