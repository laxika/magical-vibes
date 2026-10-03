package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BristlebaneBattler.class, BristlebaneOutrider.class})
class BristlebaneBattlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five -1/-1 counters")
    void entersWithFiveMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new BristlebaneBattler(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battler = findPermanent(player1, "Bristlebane Battler");

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Another creature entering under its controller's control removes a counter")
    void allyCreatureEnteringRemovesCounter() {
        Permanent battler = addBattlerWithCounters(2);

        castOutrider(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when it has no -1/-1 counters")
    void doesNotTriggerWithoutCounters() {
        Permanent battler = addBattlerWithCounters(0);

        castOutrider(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger does nothing if its last counter is removed before resolution")
    void triggerDoesNothingAfterCounterIsRemoved() {
        Permanent battler = addBattlerWithCounters(1);

        castOutrider(player1);
        harness.passBothPriorities();
        battler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature entering does not remove a counter")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent battler = addBattlerWithCounters(1);

        castOutrider(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    private Permanent addBattlerWithCounters(int count) {
        Permanent battler = harness.addToBattlefieldAndReturn(player1, new BristlebaneBattler());
        battler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return battler;
    }

    private void castOutrider(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.castFromHand(player, new BristlebaneOutrider(), "{3}{G}");
    }

    @Test
    @DisplayName("Entry counters are present immediately and its own entry does not trigger")
    void ownEntryDoesNotRemoveCounter() {
        Permanent battler = harness.enterBattlefieldAndReturn(player1, new BristlebaneBattler());

        assertThat(battler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each existing Battler removes its own counter when another Battler enters")
    void multipleBattlersRemoveTheirOwnCounters() {
        Permanent first = addBattlerWithCounters(2);
        Permanent second = addBattlerWithCounters(1);

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new BristlebaneBattler());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
    }
}
