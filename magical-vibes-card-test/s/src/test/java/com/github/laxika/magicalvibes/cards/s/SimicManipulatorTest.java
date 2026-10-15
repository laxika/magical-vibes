package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicManipulator.class, GrizzlyBears.class, DutifulThrull.class,
        FrilledOculus.class, BurstOfStrength.class, SimicCharm.class})
class SimicManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Removes the chosen number of counters and gains control of a sufficiently small creature")
    void removesChosenCountersAndGainsControl() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(manipulator.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Requires at least one counter and does not pay an illegal activation")
    void requiresAtLeastOneCounter() {
        Permanent manipulator = addReadyManipulator();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("between one");

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Checks the target's power against the chosen counter count")
    void rejectsCreatureTooPowerfulForChosenCount() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than or equal");

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles if the target's power is too high when the ability resolves")
    void fizzlesWhenTargetPowerIncreases() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void evolvesForGreaterPowerEvenWithEqualToughness() {
        Permanent manipulator = addReadyManipulator();

        harness.castFromHand(player1, new DutifulThrull(), "{W}");
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void evolvesForGreaterToughnessEvenWithEqualPower() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new FrilledOculus(), "{1}{U}");
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotEvolveForEqualPowerAndToughness() {
        Permanent manipulator = addReadyManipulator();

        harness.enterBattlefieldAndReturn(player1, new SimicManipulator());
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotEvolveForOpponentsCreature() {
        Permanent manipulator = addReadyManipulator();

        harness.enterBattlefieldAndReturn(player2, new FrilledOculus());
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void rechecksEvolveConditionAfterGrowingInResponse() {
        Permanent manipulator = addReadyManipulator();
        harness.enterBattlefieldAndReturn(player1, new DutifulThrull());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, manipulator.getId());
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void evolveUsesEnteringCreaturesStatsImmediatelyBeforeItLeaves() {
        Permanent manipulator = addReadyManipulator();
        Permanent thrull = harness.enterBattlefieldAndReturn(player1, new DutifulThrull());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BurstOfStrength(), new BurstOfStrength(), new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, manipulator.getId());
        harness.castAndResolveInstant(player1, 0, thrull.getId());
        harness.castInstant(player1, 0, 2, thrull.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Dutiful Thrull");
        resolveAllTriggers();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void rejectsRemovingMoreCountersThanAvailableWithoutPayingCosts() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new DutifulThrull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsControlIfSourceLeavesBeforeResolution() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new DutifulThrull());

        harness.activateAbility(player1, 0, 1, target.getId());
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, 2, manipulator.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Simic Manipulator");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void controlPersistsWhenSourceLeavesAfterResolution() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new DutifulThrull());

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, 2, manipulator.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Simic Manipulator");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void canTargetCreatureAlreadyControlledByAbilityController() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new DutifulThrull());

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(manipulator.isTapped()).isTrue();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void gainingControlDoesNotTriggerEvolve() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new FrilledOculus());

        harness.activateAbility(player1, 0, 1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateTapAbilityWhileSummoningSick() {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new SimicManipulator());
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new DutifulThrull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyManipulator() {
        return addCreatureReady(player1, new SimicManipulator());
    }
}
