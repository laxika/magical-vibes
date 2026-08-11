package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfiniteHourglass;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature or land puts an hourglass counter on it")
    void tappingCreatureOrLandAddsHourglassCounter() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        tapAndResolve(creature);
        tapAndResolve(land);

        assertThat(creature.getCounterCount(CounterType.HOURGLASS)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.HOURGLASS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a noncreature nonland permanent does not put an hourglass counter on it")
    void tappingNonCreatureNonLandDoesNotAddCounter() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InfiniteHourglass());

        tapAndResolve(artifact);

        assertThat(artifact.getCounterCount(CounterType.HOURGLASS)).isZero();
    }

    @Test
    @DisplayName("Hourglass counters prevent untapping and are removed from the active player's permanents at upkeep")
    void countersLockUntapAndClearForActivePlayer() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        playerOneCreature.tap();
        playerTwoCreature.tap();
        playerOneCreature.setCounterCount(CounterType.HOURGLASS, 2);
        playerTwoCreature.setCounterCount(CounterType.HOURGLASS, 1);

        advanceToNextTurn(player1);

        assertThat(playerTwoCreature.isTapped()).isTrue();
        assertThat(playerTwoCreature.getCounterCount(CounterType.HOURGLASS)).isZero();
        assertThat(playerOneCreature.getCounterCount(CounterType.HOURGLASS)).isEqualTo(2);

        advanceToNextTurn(player2);

        assertThat(playerOneCreature.isTapped()).isTrue();
        assertThat(playerOneCreature.getCounterCount(CounterType.HOURGLASS)).isZero();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
