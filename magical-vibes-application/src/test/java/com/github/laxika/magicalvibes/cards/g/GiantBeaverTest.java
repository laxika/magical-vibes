package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantBeaver.class, GrizzlyBears.class})
class GiantBeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 3 taps other creatures and saddles Giant Beaver")
    void saddleTapsOtherCreatures() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent firstHelper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondHelper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beaver.isSaddled()).isTrue();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled puts a +1/+1 counter on a creature that saddled it")
    void attacksWhileSaddledCountersSaddler() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, saddler.getId());
        harness.passBothPriorities();

        assertThat(saddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(beaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking while not saddled does not put a counter on a creature")
    void attacksWhileNotSaddledDoesNotCounter() {
        addCreatureReady(player1, new GiantBeaver());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
