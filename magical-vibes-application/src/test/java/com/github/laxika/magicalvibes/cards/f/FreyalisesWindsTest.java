package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.l.LandCap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreyalisesWinds.class, BalduvianBears.class, Forest.class, LandCap.class})
class FreyalisesWindsTest extends BaseCardTest {

    // "Whenever a permanent becomes tapped, put a wind counter on it.
    //  If a permanent with a wind counter on it would untap during its controller's untap step,
    //  remove all wind counters from it instead."

    @Test
    @DisplayName("Tapping a permanent its controller controls puts a wind counter on it")
    void tappingOwnPermanentAddsWindCounter() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a permanent an opponent controls also puts a wind counter on it")
    void tappingOpponentPermanentAddsWindCounter() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

        tapAndResolve(spider);

        assertThat(spider.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a noncreature permanent also puts a wind counter on it")
    void tappingLandAddsWindCounter() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        tapAndResolve(forest);

        assertThat(forest.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("The wind counter lands only on the permanent that became tapped")
    void windCounterLandsOnlyOnTheTappedPermanent() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new FreyalisesWinds());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());

        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.WIND)).isEqualTo(1);
        assertThat(spider.getCounterCount(CounterType.WIND)).isZero();
        assertThat(winds.getCounterCount(CounterType.WIND)).isZero();
    }

    @Test
    @DisplayName("Wind counters accumulate across separate taps")
    void windCountersAccumulate() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        tapAndResolve(bears);
        bears.untap();
        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.WIND)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing wind counters does not remove other counter types")
    void removesOnlyWindCounters() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bears.setCounterCount(CounterType.WIND, 2);
        bears.tap();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.WIND)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wind counters remain when another effect prevents the permanent from untapping")
    void keepsWindCountersWhenUntapIsPrevented() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent landCap = harness.addToBattlefieldAndReturn(player1, new LandCap());
        landCap.setCounterCount(CounterType.DEPLETION, 1);
        landCap.setCounterCount(CounterType.WIND, 1);
        landCap.tap();

        advanceToUpkeep(player1);

        assertThat(landCap.isTapped()).isTrue();
        assertThat(landCap.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("A permanent with wind counters does not untap; all its wind counters are removed instead")
    void windCounteredPermanentDoesNotUntap() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.setCounterCount(CounterType.WIND, 2);
        spider.tap();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.WIND)).isZero();
    }

    @Test
    @DisplayName("Once its wind counters are gone the permanent untaps on the following untap step")
    void untapsAfterWindCountersRemoved() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.setCounterCount(CounterType.WIND, 1);
        spider.tap();

        advanceToUpkeep(player2);
        assertThat(spider.isTapped()).isTrue();

        advanceToUpkeep(player1);
        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped permanent without wind counters untaps normally")
    void permanentWithoutWindCountersUntaps() {
        harness.addToBattlefield(player1, new FreyalisesWinds());
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.tap();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Freyalise's Winds on the battlefield the untap replacement does not apply")
    void noReplacementWithoutTheEnchantment() {
        Permanent spider = addCreatureReady(player2, new BalduvianBears());
        spider.setCounterCount(CounterType.WIND, 1);
        spider.tap();

        advanceToUpkeep(player2);

        assertThat(spider.isTapped()).isFalse();
        assertThat(spider.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

}
