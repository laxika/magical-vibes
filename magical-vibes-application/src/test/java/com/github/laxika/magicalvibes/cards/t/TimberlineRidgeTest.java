package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimberlineRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for red adds {R} and puts a depletion counter on the land")
    void tapsForRedAndAddsDepletionCounter() {
        Permanent ridge = addTimberlineRidge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green adds {G} and puts a depletion counter on the land")
    void tapsForGreenAndAddsDepletionCounter() {
        Permanent ridge = addTimberlineRidge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Timberline Ridge with a depletion counter stays tapped through the untap step, then the upkeep trigger removes the counter")
    void doesNotUntapWithDepletionCounterThenUpkeepRemovesIt() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();
        ridge.setCounterCount(CounterType.DEPLETION, 1);

        advanceToPlayerOneUpkeep();

        assertThat(ridge.isTapped()).isTrue();
        assertThat(ridge.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Timberline Ridge with no depletion counter untaps normally")
    void untapsWithoutDepletionCounter() {
        Permanent ridge = addTimberlineRidge();
        ridge.tap();

        advanceToPlayerOneUpkeep();

        assertThat(ridge.isTapped()).isFalse();
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

    private Permanent addTimberlineRidge() {
        Permanent ridge = harness.addToBattlefieldAndReturn(player1, new TimberlineRidge());
        ridge.setSummoningSick(false);
        return ridge;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
