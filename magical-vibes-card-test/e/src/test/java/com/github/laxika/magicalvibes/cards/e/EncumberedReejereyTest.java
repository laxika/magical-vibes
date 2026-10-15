package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EncumberedReejerey.class})
class EncumberedReejereyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EncumberedReejerey(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent reejerey = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Becoming tapped removes a -1/-1 counter")
    void becomingTappedRemovesCounter() {
        Permanent reejerey = addReejereyWithCounters(1);

        tap(reejerey);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Becoming tapped without a -1/-1 counter does not trigger")
    void becomingTappedWithoutCounterDoesNotTrigger() {
        Permanent reejerey = addReejereyWithCounters(0);

        tap(reejerey);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger does nothing if the counter condition is no longer met")
    void triggerDoesNothingAfterCounterIsRemoved() {
        Permanent reejerey = addReejereyWithCounters(1);

        tap(reejerey);
        reejerey.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking removes exactly one counter after the tap trigger resolves")
    void attackingRemovesExactlyOneCounter() {
        Permanent reejerey = addCreatureReady(player1, new EncumberedReejerey());
        reejerey.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        declareAttackers(List.of(0));

        assertThat(reejerey.isTapped()).isTrue();
        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another copy removes only that copy's counter")
    void tappingAnotherCopyDoesNotTriggerThisCopy() {
        Permanent first = addReejereyWithCounters(3);
        Permanent second = addReejereyWithCounters(3);

        tap(second);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Reejerey triggers for its own attack")
    void opponentReejereyTriggersForItsOwnAttack() {
        Permanent reejerey = addCreatureReady(player2, new EncumberedReejerey());
        reejerey.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapping before resolution does not prevent counter removal")
    void triggerStillRemovesCounterAfterUntapping() {
        Permanent reejerey = addReejereyWithCounters(3);

        tap(reejerey);
        reejerey.untap();
        resolveAllTriggers();

        assertThat(reejerey.isTapped()).isFalse();
        assertThat(reejerey.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private Permanent addReejereyWithCounters(int counterCount) {
        Permanent reejerey = harness.addToBattlefieldAndReturn(player1, new EncumberedReejerey());
        reejerey.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, counterCount);
        return reejerey;
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
