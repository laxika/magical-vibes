package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PulseOfTheFields;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgelessEntity.class, PulseOfTheFields.class})
class AgelessEntityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts as many +1/+1 counters on itself as life gained")
    void putsCountersEqualToLifeGained() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(entity.getEffectivePower()).isEqualTo(8);
        assertThat(entity.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not trigger when only an opponent gains life")
    void doesNotTriggerForOpponentLifeGain() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());

        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 16);

        harness.castFromHand(player2, new PulseOfTheFields(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers separately for each life-gain event")
    void triggersForEachLifeGainEvent() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Life gain puts a separate ability on the stack before counters are added")
    void countersWaitForTriggeredAbilityToResolve() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());

        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(entity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each controlled Entity gets the full life-gain amount even above starting life")
    void eachEntityGetsCountersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AgelessEntity());
        Permanent opponentEntity = harness.addToBattlefieldAndReturn(player2, new AgelessEntity());
        harness.setLife(player1, 30);

        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 34);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentEntity.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
