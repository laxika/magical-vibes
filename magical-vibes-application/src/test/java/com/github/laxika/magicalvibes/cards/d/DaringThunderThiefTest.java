package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaringThunderThief.class})
class DaringThunderThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new DaringThunderThief(), "{3}{U}");
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        Permanent thief = findPermanent(player1, "Daring Thunder-Thief");
        assertThat(thief.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash allows it to be cast during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new DaringThunderThief(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step and it still enters tapped")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DaringThunderThief(), "{3}{U}");
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(findPermanent(player1, "Daring Thunder-Thief").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters tapped without being cast and untaps normally on its controller's turn")
    void entersTappedWithoutCastingAndUntapsNormally() {
        Permanent thief = harness.enterBattlefieldAndReturn(player1, new DaringThunderThief());

        assertThat(thief.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player2);
        assertThat(thief.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(thief.isTapped()).isFalse();
    }
}
