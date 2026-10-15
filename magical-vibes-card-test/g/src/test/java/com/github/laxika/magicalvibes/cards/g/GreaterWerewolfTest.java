package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfBone;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterWerewolf.class, WallOfBone.class, GiantSpider.class, GiantGrowth.class})
class GreaterWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Each blocker gets a -0/-2 counter at end of combat")
    void eachBlockerGetsCounterAtEndOfCombat() {
        addCreatureReady(player1, new GreaterWerewolf());
        Permanent firstBlocker = addCreatureReady(player2, new WallOfBone());
        Permanent secondBlocker = addCreatureReady(player2, new WallOfBone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2));
        leaveEndOfCombat();

        assertThat(firstBlocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(secondBlocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("The blocker gets a -0/-2 counter reducing its toughness at end of combat")
    void blockerToughnessReducedAtEndOfCombat() {
        addCreatureReady(player1, new GreaterWerewolf());
        Permanent wall = addCreatureReady(player2, new WallOfBone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();

        leaveEndOfCombat();

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature blocked by Greater Werewolf gets a -0/-2 counter at end of combat")
    void blockedAttackerGetsCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new GreaterWerewolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when Greater Werewolf neither blocks nor is blocked")
    void noCounterWhenNotInCombat() {
        addCreatureReady(player1, new GreaterWerewolf());
        Permanent wall = addCreatureReady(player2, new WallOfBone());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    @Test
    @DisplayName("The ability does not trigger if Greater Werewolf leaves before end of combat")
    void noCounterIfWerewolfLeavesBeforeEndOfCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        Permanent blocker = addCreatureReady(player2, new WallOfBone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, werewolf));
        leaveEndOfCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    @Test
    @DisplayName("The ability still resolves if Greater Werewolf leaves after its trigger is on the stack")
    void resolvesIfWerewolfLeavesAfterTriggerIsCreated() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        Permanent blocker = addCreatureReady(player2, new WallOfBone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, werewolf));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("The end-of-combat ability triggers even when Greater Werewolf did not attack or block")
    void triggersWithoutParticipatingInCombat() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(werewolf.getId());
    }

    @Test
    @DisplayName("A blocker that regenerates out of combat does not receive a counter")
    void regeneratedBlockerDoesNotReceiveCounter() {
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        Permanent wall = addCreatureReady(player2, new WallOfBone());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.castAndResolveInstant(player1, 0, werewolf.getId());
            harness.activateAbility(player2, 0, null, null);
            harness.passBothPriorities();
        });
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Wall of Bone");
        assertThat(wall.isBlocking()).isFalse();
        resolveAllTriggers();

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    private void leaveEndOfCombat() {
        if (gd.currentStep != TurnStep.END_OF_COMBAT) {
            harness.passUntil(TurnStep.END_OF_COMBAT);
        }
        harness.passBothPriorities();
    }
}
