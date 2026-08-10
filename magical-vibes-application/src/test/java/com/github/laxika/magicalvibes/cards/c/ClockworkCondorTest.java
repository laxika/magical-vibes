package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClockworkCondorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters, making it a 3/3")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new ClockworkCondor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent condor = findPermanent(player1, "Clockwork Condor");
        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, condor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking removes a +1/+1 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, condor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking removes a +1/+1 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent condor = addCreatureReady(player2, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        leaveEndOfCombat();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not remove a counter when it neither attacks nor blocks")
    void doesNothingWhenNotInCombat() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of());
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
