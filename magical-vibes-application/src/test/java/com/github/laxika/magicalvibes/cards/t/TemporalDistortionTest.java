package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemporalDistortion.class, RagingKavu.class, Forest.class, ChromaticSphere.class})
class TemporalDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature or land puts an hourglass counter on it")
    void tappingCreatureOrLandAddsHourglassCounter() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());

        tapAndResolve(artifact);

        assertThat(artifact.getCounterCount(CounterType.HOURGLASS)).isZero();
    }

    @Test
    @DisplayName("Hourglass counters prevent untapping and are removed from the active player's permanents at upkeep")
    void countersLockUntapAndClearForActivePlayer() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        playerOneCreature.tap();
        playerTwoCreature.tap();
        playerOneCreature.setCounterCount(CounterType.HOURGLASS, 2);
        playerTwoCreature.setCounterCount(CounterType.HOURGLASS, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(playerTwoCreature.isTapped()).isTrue();
        assertThat(playerTwoCreature.getCounterCount(CounterType.HOURGLASS)).isZero();
        assertThat(playerOneCreature.getCounterCount(CounterType.HOURGLASS)).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(playerOneCreature.isTapped()).isTrue();
        assertThat(playerOneCreature.getCounterCount(CounterType.HOURGLASS)).isZero();
    }

    @Test
    @DisplayName("An hourglass counter locks a noncreature permanent until its controller's upkeep")
    void counterLocksNoncreaturePermanentUntilControllerUpkeep() {
        harness.addToBattlefield(player1, new TemporalDistortion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());
        artifact.tap();
        artifact.setCounterCount(CounterType.HOURGLASS, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.HOURGLASS)).isZero();

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @CardUsed(MinimusContainment.class)
    @DisplayName("Losing its abilities stops the opponent-permanent tap trigger")
    void losingAbilitiesStopsOpponentPermanentTapTrigger() {
        Permanent distortion = harness.addToBattlefieldAndReturn(player1, new TemporalDistortion());
        Permanent containment = harness.addToBattlefieldAndReturn(player2, new MinimusContainment());
        containment.setAttachedTo(distortion.getId());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RagingKavu());

        assertThat(gqs.hasLostAllAbilities(gd, distortion)).isTrue();

        opponentCreature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, opponentCreature));

        assertThat(gd.stack).isEmpty();
        assertThat(opponentCreature.getCounterCount(CounterType.HOURGLASS)).isZero();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

}
