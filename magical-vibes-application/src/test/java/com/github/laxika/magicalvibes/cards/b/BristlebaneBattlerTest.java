package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BristlebaneBattlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five -1/-1 counters")
    void entersWithFiveMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BristlebaneBattler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battler = findPermanent(player1, "Bristlebane Battler");

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Another creature entering under its controller's control removes a counter")
    void allyCreatureEnteringRemovesCounter() {
        Permanent battler = addBattlerWithCounters(2);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when it has no -1/-1 counters")
    void doesNotTriggerWithoutCounters() {
        Permanent battler = addBattlerWithCounters(0);

        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger does nothing if its last counter is removed before resolution")
    void triggerDoesNothingAfterCounterIsRemoved() {
        Permanent battler = addBattlerWithCounters(1);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        battler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature entering does not remove a counter")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent battler = addBattlerWithCounters(1);

        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    private Permanent addBattlerWithCounters(int count) {
        Permanent battler = harness.addToBattlefieldAndReturn(player1, new BristlebaneBattler());
        battler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return battler;
    }

    private void castGrizzlyBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
