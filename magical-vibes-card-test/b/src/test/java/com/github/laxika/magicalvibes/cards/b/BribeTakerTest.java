package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BribeTaker.class, SakuraTribeElder.class})
class BribeTakerTest extends BaseCardTest {

    @Test
    void putsChosenCounterForEachControlledCounterKind() {
        Permanent chargePermanent = addCreatureReady(player1, new SakuraTribeElder());
        Permanent plusOnePermanent = addCreatureReady(player1, new SakuraTribeElder());
        chargePermanent.setCounterCount(CounterType.CHARGE, 1);
        plusOnePermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = findPermanent(player1, "Bribe Taker");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayDeclineEachCounterKind() {
        Permanent support = addCreatureReady(player1, new SakuraTribeElder());
        support.setCounterCount(CounterType.CHARGE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = findPermanent(player1, "Bribe Taker");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "Don't put a counter");

        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsOnlyPermanentsControlledByTheSourceController() {
        Permanent opponentPermanent = addCreatureReady(player2, new SakuraTribeElder());
        opponentPermanent.setCounterCount(CounterType.CHARGE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = findPermanent(player1, "Bribe Taker");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsEachKindOnceRegardlessOfCounterQuantityOrPermanentCount() {
        Permanent first = addCreatureReady(player1, new SakuraTribeElder());
        Permanent second = addCreatureReady(player1, new SakuraTribeElder());
        first.setCounterCount(CounterType.SHIELD, 2);
        second.setCounterCount(CounterType.SHIELD, 3);

        castBribeTaker();
        resolveAllTriggers();
        harness.handleListChoice(player1, "shield counters");

        assertThat(findPermanent(player1, "Bribe Taker").getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChoosePlusOneCountersForEveryDifferentKindWithoutCountingNewKinds() {
        Permanent support = addCreatureReady(player1, new SakuraTribeElder());
        support.setCounterCount(CounterType.CHARGE, 1);
        support.setCounterCount(CounterType.SHIELD, 1);

        castBribeTaker();
        resolveAllTriggers();
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        Permanent bribeTaker = findPermanent(player1, "Bribe Taker");
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bribeTaker.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsCountersOnItselfAddedBeforeTheTriggerResolves() {
        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = findPermanent(player1, "Bribe Taker");
        bribeTaker.setCounterCount(CounterType.SHIELD, 1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "shield counters");

        assertThat(bribeTaker.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ignoresCounterKindsRemovedBeforeResolution() {
        Permanent support = addCreatureReady(player1, new SakuraTribeElder());
        support.setCounterCount(CounterType.CHARGE, 1);
        castBribeTaker();
        harness.passBothPriorities();
        support.setCounterCount(CounterType.CHARGE, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Bribe Taker").getCounterCount(CounterType.CHARGE)).isZero();
    }

    private void castBribeTaker() {
        harness.setHand(player1, List.of(new BribeTaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

}
