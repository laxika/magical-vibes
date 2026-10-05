package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MedicineRunner.class, GrizzlyBears.class, Forest.class})
class MedicineRunnerTest extends BaseCardTest {

    /**
     * Casts Medicine Runner, resolves it onto the battlefield, accepts the may ability and chooses
     * {@code target} so the ETB triggered ability is placed on the stack, then resolves it.
     */
    private void castAcceptAndResolve(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MedicineRunner()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Accepting the may ability removes a +1/+1 counter from the target")
    void removesPlusOnePlusOneCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAcceptAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter of a non +1/+1 kind (charge)")
    void removesChargeCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 3);

        castAcceptAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability leaves counters untouched")
    void decliningLeavesCountersUntouched() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MedicineRunner()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving against a target with no counters is a harmless no-op")
    void noCounterIsNoOp() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAcceptAndResolve(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Medicine Runner");
    }

    @Test
    @DisplayName("Multiple counter types require the ability controller to choose before removal")
    void choosesCounterTypeAtResolution() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 2);
        forest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castAcceptAndResolve(forest);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(forest.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A noncreature permanent is a legal target")
    void removesCounterFromLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 2);

        castAcceptAndResolve(forest);

        assertThat(forest.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent controlled by the ability controller is a legal target")
    void removesCounterFromOwnPermanent() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new MedicineRunner());
        runner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAcceptAndResolve(runner);

        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An ability with a target that left the battlefield does not resolve")
    void targetLeavingBeforeResolutionPreventsRemoval() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MedicineRunner()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());
        gd.playerBattlefields.get(player2.getId()).remove(forest);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(forest.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Medicine Runner");
    }
}
