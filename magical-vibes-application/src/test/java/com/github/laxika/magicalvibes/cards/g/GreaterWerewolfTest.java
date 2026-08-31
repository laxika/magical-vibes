package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterWerewolf.class, GiantOyster.class})
class GreaterWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Each blocker gets a -0/-2 counter at end of combat")
    void eachBlockerGetsCounterAtEndOfCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        werewolf.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GiantOyster());
        Permanent secondBlocker = addCreatureReady(player2, new GiantOyster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2));
        leaveEndOfCombat();

        assertThat(firstBlocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(secondBlocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("The blocker gets a -0/-2 counter reducing its toughness at end of combat")
    void blockerToughnessReducedAtEndOfCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        werewolf.setAttacking(true);
        Permanent oyster = addCreatureReady(player2, new GiantOyster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(oyster.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();

        leaveEndOfCombat();

        assertThat(oyster.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, oyster)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, oyster)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature blocked by Greater Werewolf gets a -0/-2 counter at end of combat")
    void blockedAttackerGetsCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GiantOyster());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GreaterWerewolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when Greater Werewolf neither blocks nor is blocked")
    void noCounterWhenNotInCombat() {
        addCreatureReady(player1, new GreaterWerewolf());
        Permanent oyster = addCreatureReady(player2, new GiantOyster());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(StepTriggerService.class).handleEndOfCombatTriggers(gd));
        leaveEndOfCombat();

        assertThat(oyster.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    @Test
    @DisplayName("The ability does not trigger if Greater Werewolf leaves before end of combat")
    void noCounterIfWerewolfLeavesBeforeEndOfCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        werewolf.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantOyster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, werewolf));
        leaveEndOfCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    private void leaveEndOfCombat() {
        if (gd.currentStep != TurnStep.END_OF_COMBAT) {
            harness.passUntil(TurnStep.END_OF_COMBAT);
        }
        harness.passBothPriorities();
    }
}
