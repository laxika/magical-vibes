package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Sets base toughness to one plus the blocking creature's power")
    void setsBaseToughnessFromBlockingCreaturePower() {
        Permanent sentinel = addReadySentinel(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(4);
        harness.addToBattlefield(player2, bears);
        Permanent attacker = findPermanent(player2, "Grizzly Bears");

        setupSentinelBlockingAttacker(sentinel, attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(sentinel.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sets base toughness from a creature blocking Sentinel")
    void setsBaseToughnessFromCreatureBlockingSentinel() {
        Permanent sentinel = addReadySentinel(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        sentinel.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        blocker.addBlockingTarget(sentinelIndex);
        blocker.addBlockingTargetId(sentinel.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(sentinel.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Locks the target's power at resolution and survives cleanup")
    void locksPowerAndSurvivesCleanup() {
        Permanent sentinel = addReadySentinel(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        setupSentinelAttackingBlockedBy(sentinel, blocker);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature not blocking or blocked by Sentinel")
    void cannotTargetCreatureNotInCombat() {
        Permanent sentinel = addReadySentinel(player1);
        sentinel.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySentinel(Player player) {
        Permanent perm = new Permanent(new Sentinel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupSentinelBlockingAttacker(Permanent sentinel, Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        sentinel.setBlocking(true);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        sentinel.addBlockingTarget(attackerIndex);
        sentinel.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void setupSentinelAttackingBlockedBy(Permanent sentinel, Permanent blocker) {
        sentinel.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        blocker.addBlockingTarget(sentinelIndex);
        blocker.addBlockingTargetId(sentinel.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
