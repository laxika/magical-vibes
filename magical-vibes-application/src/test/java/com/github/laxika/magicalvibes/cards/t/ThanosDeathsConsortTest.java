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
        harness.addToBattlefield(player1, new ThanosDeathsConsort());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent thanos = findPermanent(player1, "Thanos, Death's Consort");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsACounterWhenAnAllyCreatureDies() {
        harness.addToBattlefield(player1, new ThanosDeathsConsort());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent thanos = findPermanent(player1, "Thanos, Death's Consort");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
