package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeltCollectorTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenLargerCreatureEnters() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenEnteringCreatureIsNotLarger() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checksPowerAgainWhenEnterTriggerResolves() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItselfWhenLargerCreatureDies() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).contains(pelt);
        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainsTrampleWithThreePlusOnePlusOneCounters() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isFalse();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isTrue();
    }
}
