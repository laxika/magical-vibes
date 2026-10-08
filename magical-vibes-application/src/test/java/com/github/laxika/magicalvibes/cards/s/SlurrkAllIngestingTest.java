package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlurrkAllIngesting.class, GrizzlyBears.class})
class SlurrkAllIngestingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters")
    void entersWithFiveCounters() {
        harness.castFromHand(player1, new SlurrkAllIngesting(), "{5}{G}");
        harness.passBothPriorities();

        Permanent slurrk = findPermanent(player1, "Slurrk, All-Ingesting");

        assertThat(slurrk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("A countered creature you control dying grows each controlled creature with a counter")
    void growsCounteredControlledCreaturesWhenCounteredAllyDies() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent counteredBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        kill(counteredBear);

        assertThat(slurrk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(uncounteredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when the dying creature has no +1/+1 counter")
    void doesNotTriggerForUncounteredAlly() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        kill(bear);

        assertThat(slurrk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger for a countered creature an opponent controls")
    void doesNotTriggerForOpponentCreature() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        kill(opponentBear);

        assertThat(slurrk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers when Slurrk itself dies with a +1/+1 counter")
    void growsOtherCounteredCreatureWhenSlurrkDies() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent counteredBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        kill(slurrk);

        assertThat(counteredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Slurrk, All-Ingesting");
    }

    @Test
    @DisplayName("Each simultaneous countered death triggers, including Slurrk's own death")
    void triggersForEachSimultaneousDeath() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent dyingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        survivor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        slurrk.setMarkedDamage(slurrk.getEffectiveToughness());
        dyingBear.setMarkedDamage(dyingBear.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Slurrk, All-Ingesting");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counter eligibility is checked when the death trigger resolves")
    void checksRecipientsAtResolution() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent dyingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent losingCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent gainingCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dyingBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dyingBear.setMarkedDamage(dyingBear.getEffectiveToughness());
        harness.runStateBasedActions();

        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gainingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(slurrk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(losingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gainingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Slurrk dying without a +1/+1 counter does not grow other creatures")
    void doesNotTriggerForUncounteredSelfDeath() {
        Permanent slurrk = addSlurrkWithCounters();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        slurrk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Slurrk, All-Ingesting");
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }

    private Permanent addSlurrkWithCounters() {
        Permanent slurrk = harness.addToBattlefieldAndReturn(player1, new SlurrkAllIngesting());
        slurrk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        return slurrk;
    }
}
