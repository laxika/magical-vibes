package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwornDefenderTest extends BaseCardTest {

    @Test
    @DisplayName("Power becomes blocker's toughness minus 1 and toughness becomes blocker's power plus 1")
    void copiesStatsFromBlocker() {
        Permanent defender = addReadyDefender(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setToughness(5);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Blocker is 2/5 -> Sworn Defender becomes 4/3
        assertThat(defender.getEffectivePower()).isEqualTo(4);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Works against a creature it is blocking")
    void copiesStatsFromAttackerItBlocks() {
        Permanent defender = addReadyDefender(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(4);
        harness.addToBattlefield(player2, bears);
        Permanent attacker = findPermanent(player2, "Grizzly Bears");

        setupDefenderBlockingAttacker(defender, attacker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // Attacker is 4/2 -> Sworn Defender becomes 1/5
        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Uses the target's current, boosted power and toughness")
    void usesBoostedStatsOfTarget() {
        Permanent defender = addReadyDefender(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // 4/4

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(3);
        assertThat(defender.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Values are locked in at resolution — later changes to the target do not update them")
    void locksInValuesAtResolution() {
        Permanent defender = addReadyDefender(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature not blocking or blocked by it")
    void cannotTargetCreatureNotInCombat() {
        Permanent defender = addReadyDefender(player1);
        defender.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effect wears off at cleanup")
    void wearsOffAtCleanup() {
        Permanent defender = addReadyDefender(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setToughness(5);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        setupDefenderAttackingBlockedBy(defender, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(defender.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(defender.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(defender.getEffectivePower()).isEqualTo(1);
        assertThat(defender.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addReadyDefender(Player player) {
        Permanent perm = new Permanent(new SwornDefender());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDefenderAttackingBlockedBy(Permanent defender, Permanent blocker) {
        defender.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        int defenderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(defender);
        blocker.addBlockingTarget(defenderIndex);
        blocker.addBlockingTargetId(defender.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void setupDefenderBlockingAttacker(Permanent defender, Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        defender.setBlocking(true);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        defender.addBlockingTarget(attackerIndex);
        defender.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
