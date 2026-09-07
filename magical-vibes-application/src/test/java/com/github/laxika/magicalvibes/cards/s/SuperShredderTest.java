package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperShredder.class, GrizzlyBears.class, Forest.class})
class SuperShredderTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureLeavesTheBattlefield() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenAnotherNoncreaturePermanentLeavesTheBattlefield() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        harness.passBothPriorities();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
