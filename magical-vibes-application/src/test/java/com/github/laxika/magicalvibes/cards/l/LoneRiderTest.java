package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoneRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at the end step when its controller gained exactly 3 life")
    void transformsAtThreeLifeGained() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when its controller gained fewer than 3 life")
    void doesNotTransformBelowLifeThreshold() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not transform from an opponent's life gain")
    void opponentLifeGainDoesNotTransform() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        gd.lifeGainedThisTurn.put(player2.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isFalse();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
