package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranHomeGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives it a -0/-1 counter and a Deserter token at end of combat")
    void attackingPutsCounterAndCreatesToken() {
        Permanent guard = addCreatureReady(player1, new KjeldoranHomeGuard());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(5);

        Permanent token = findPermanent(player1, "Deserter");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking gives it a -0/-1 counter and a Deserter token, but only at end of combat")
    void blockingPutsCounterAndCreatesToken() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent guard = addCreatureReady(player2, new KjeldoranHomeGuard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Deserter")).isNull();

        leaveEndOfCombat();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Deserter")).isNotNull();
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void nothingWhenNotInCombat() {
        Permanent guard = addCreatureReady(player1, new KjeldoranHomeGuard());

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)).isFalse();

        leaveEndOfCombat();

        assertThat(guard.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Deserter")).isNull();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
