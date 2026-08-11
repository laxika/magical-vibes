package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EncumberedReejereyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EncumberedReejerey()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
