package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GideonsAvengerTest extends BaseCardTest {

    // "Whenever a creature an opponent controls becomes tapped, put a +1/+1 counter on this creature."

    @Test
    @DisplayName("An opponent's creature becoming tapped puts a +1/+1 counter on Gideon's Avenger")
    void opponentCreatureTapAddsCounter() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(bears);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent creature tap adds another counter")
    void countersAccumulate() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(first);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        tap(second);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping your own creature does not trigger")
    void ownCreatureTapDoesNotTrigger() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapping an opponent's noncreature permanent does not trigger")
    void opponentLandTapDoesNotTrigger() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
