package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulSwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get counters without delirium")
    void doesNotGetCountersWithoutDelirium() {
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets three +1/+1 counters with delirium")
    void getsThreeCountersWithDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
