package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CordialVampire.class, ChildOfNight.class, GrizzlyBears.class})
class CordialVampireTest extends BaseCardTest {

    @Test
    void anotherCreatureDiesPutsCountersOnEachVampireYouControl() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent child = addCreatureReady(player1, new ChildOfNight());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        destroy(bears);

        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void thisCreatureDyingPutsCounterOnRemainingVampiresYouControl() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent child = addCreatureReady(player1, new ChildOfNight());

        destroy(cordial);

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cordial Vampire");
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        harness.passBothPriorities();
    }
}
