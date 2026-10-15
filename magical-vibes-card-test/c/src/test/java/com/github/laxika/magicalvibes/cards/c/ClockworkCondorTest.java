package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClockworkCondor.class, Ornithopter.class})
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

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, condor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking keeps its counters through combat damage")
    void attackingKeepsCountersUntilEndOfCombat() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking removes a +1/+1 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        addCreatureReady(player1, new Ornithopter());
        Permanent condor = addCreatureReady(player2, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not remove a counter when it neither attacks nor blocks")
    void doesNothingWhenNotInCombat() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("End-of-combat counter removal uses the stack before shrinking the creature")
    void counterRemovalWaitsForDelayedTriggerToResolve() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(condor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked Condor deals damage before losing its last counter and dying")
    void lastCounterIsRemovedAfterCombatDamage() {
        Permanent condor = addCreatureReady(player1, new ClockworkCondor());
        condor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(condor);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(condor.getCard());
    }

}
