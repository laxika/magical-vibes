package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThanosDeathsConsort.class, GrizzlyBears.class, Shock.class})
class ThanosDeathsConsortTest extends BaseCardTest {

    @Test
    void getsACounterWhenAnotherCreatureDies() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsACounterWhenAnAllyCreatureDies() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenDamageIsNotLethal() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, thanos.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thanos, Death's Consort");
        assertThat(gd.stack).isEmpty();
        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerOnItsOwnDeath() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, thanos.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, thanos.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thanos, Death's Consort");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachDeathQueuesASeparateCounterTrigger() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opponent.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void pendingCounterTriggerDoesNothingAfterThanosDies() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosDeathsConsort());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, thanos.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, thanos.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thanos, Death's Consort");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
