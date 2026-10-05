package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoneRider.class})
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

    @Test
    @DisplayName("Transforms during an opponent's end step from its controller's life gain")
    void transformsDuringOpponentsEndStep() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Counts multiple life-gain events even when life is subsequently lost")
    void countsTotalLifeGainedRatherThanNetLifeChange() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 5, "life loss"));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Life gained after the end step begins cannot create the transform trigger")
    void gainingLifeTooLateDoesNotTrigger() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Life gained before Lone Rider enters still counts toward transforming it")
    void countsLifeGainedBeforeEntering() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("It That Rides as One has no end-step ability to transform back")
    void backFaceDoesNotTransformAgain() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new LoneRider());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(rider.isTransformed()).isTrue();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(rider.isTransformed()).isTrue();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
