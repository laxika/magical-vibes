package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WormfangManta.class)
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
}
