package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LandCapTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for white adds {W} and puts a depletion counter on the land")
    void tapsForWhiteAndAddsDepletionCounter() {
        Permanent landCap = addLandCap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.WHITE)).isEqualTo(1);
        assertThat(landCap.isTapped()).isTrue();
        assertThat(landCap.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and puts a depletion counter on the land")
    void tapsForBlueAndAddsDepletionCounter() {
        Permanent landCap = addLandCap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(landCap.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Land Cap with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent landCap = addLandCap();
        landCap.tap();
        landCap.setCounterCount(CounterType.DEPLETION, 1);

        advanceToPlayerOneUpkeep();

        assertThat(landCap.isTapped()).isTrue();
        assertThat(landCap.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("A Land Cap with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent landCap = addLandCap();
        landCap.tap();

        advanceToPlayerOneUpkeep();

        assertThat(landCap.isTapped()).isFalse();
    }

    /**
     * Ends player2's turn so play cascades into player1's untap step and then their upkeep, where
     * the depletion-counter removal trigger resolves.
     */
    private void advanceToPlayerOneUpkeep() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addLandCap() {
        Permanent landCap = harness.addToBattlefieldAndReturn(player1, new LandCap());
        landCap.setSummoningSick(false);
        return landCap;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
