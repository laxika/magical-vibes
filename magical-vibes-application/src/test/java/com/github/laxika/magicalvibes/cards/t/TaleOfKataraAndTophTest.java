package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaleOfKataraAndToph.class, GrizzlyBears.class})
class TaleOfKataraAndTophTest extends BaseCardTest {

    @Test
    void putsACounterOnEachCreatureTheFirstTimeItBecomesTappedDuringYourTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        tapAndCollect(first);
        tapAndCollect(second);
        resolveAllTriggersDirectly();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAgainForTheSameCreatureThatTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        tapAndCollect(creature);
        resolveAllTriggersDirectly();
        creature.untap();
        tapAndCollect(creature);
        resolveAllTriggersDirectly();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void tapAndCollect(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    private void resolveAllTriggersDirectly() {
        while (!gd.stack.isEmpty()) {
            harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        }
    }
}
