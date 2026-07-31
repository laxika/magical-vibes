package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShieldSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking immediately puts a -0/-1 counter on it")
    void blockingPutsMinusZeroMinusOneCounter() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        block();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sphere)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking on a later turn stacks another counter")
    void countersAccumulateAcrossBlocks() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        block();
        sphere.setBlocking(false);
        attacker.setAttacking(true);
        block();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sitting on the battlefield without blocking gives no counter")
    void noCounterWithoutBlocking() {
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
    }

    private void block() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
