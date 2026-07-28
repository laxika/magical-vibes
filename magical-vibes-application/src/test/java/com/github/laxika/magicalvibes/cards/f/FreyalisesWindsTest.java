package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreyalisesWindsTest extends BaseCardTest {

    // "Whenever a permanent becomes tapped, put a wind counter on it.
    //  If a permanent with a wind counter on it would untap during its controller's untap step,
    //  remove all wind counters from it instead."

    @Test
    @DisplayName("Tapping a permanent its controller controls puts a wind counter on it")
    void tappingOwnPermanentAddsWindCounter() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a permanent an opponent controls also puts a wind counter on it")
    void tappingOpponentPermanentAddsWindCounter() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        tapAndResolve(spider);

        assertThat(spider.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wind counters accumulate across separate taps")
    void windCountersAccumulate() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tapAndResolve(bears);
        bears.untap();
        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.WIND)).isEqualTo(2);
    }

    @Test
    @DisplayName("A permanent with wind counters does not untap; all its wind counters are removed instead")
    void windCounteredPermanentDoesNotUntap() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setCounterCount(CounterType.WIND, 2);
        spider.tap();

        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.WIND)).isZero();
    }

    @Test
    @DisplayName("Once its wind counters are gone the permanent untaps on the following untap step")
    void untapsAfterWindCountersRemoved() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setCounterCount(CounterType.WIND, 1);
        spider.tap();

        advanceToNextTurn(player1);
        assertThat(spider.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped permanent without wind counters untaps normally")
    void permanentWithoutWindCountersUntaps() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.tap();

        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Freyalise's Winds on the battlefield the untap replacement does not apply")
    void noReplacementWithoutTheEnchantment() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setCounterCount(CounterType.WIND, 1);
        spider.tap();

        advanceToNextTurn(player1);

        assertThat(spider.isTapped()).isFalse();
        assertThat(spider.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
