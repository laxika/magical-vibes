package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralJarkeld.class, KjeldoranWarrior.class})
class GeneralJarkeldTest extends BaseCardTest {

    @Test
    @DisplayName("Swaps exclusive blockers between two blocked attackers")
    void swapsExclusiveBlockers() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent attackerA = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent attackerB = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent blockerA = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent blockerB = addCreatureReady(player2, new KjeldoranWarrior());

        setupTwoBlockedAttackers(attackerA, attackerB, blockerA, blockerB);

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(attackerA.getId(), attackerB.getId()));
        harness.passBothPriorities();

        assertThat(jarkeld.isTapped()).isTrue();
        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(blockerA.getBlockingTargets()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).indexOf(attackerB));
        assertThat(blockerB.getBlockingTargets()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).indexOf(attackerA));
        assertThat(blockerA.isBlocking()).isTrue();
        assertThat(blockerB.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shared blockers stay put; only exclusive blockers swap")
    void sharedBlockersRemain() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent attackerA = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent attackerB = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent exclusiveA = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent exclusiveB = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent shared = addCreatureReady(player2, new KjeldoranWarrior());

        int aIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerA);
        int bIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerB);

        attackerA.setAttacking(true);
        attackerB.setAttacking(true);
        assignBlocker(exclusiveA, attackerA, aIdx);
        assignBlocker(exclusiveB, attackerB, bIdx);
        // Shared blocker blocks both (manual: creatures normally block only one without a grant).
        shared.setBlocking(true);
        shared.addBlockingTarget(aIdx);
        shared.addBlockingTargetId(attackerA.getId());
        shared.addBlockingTarget(bIdx);
        shared.addBlockingTargetId(attackerB.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(attackerA.getId(), attackerB.getId()));
        harness.passBothPriorities();

        assertThat(exclusiveA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(exclusiveB.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(shared.getBlockingTargetIds()).containsExactlyInAnyOrder(attackerA.getId(), attackerB.getId());
    }

    @Test
    @DisplayName("Does nothing when mutual block legality fails (flying)")
    void noSwapWhenLegalityFails() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent groundAttacker = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent flyingAttacker = addCreatureReady(player1, new KjeldoranWarrior());
        flyingAttacker.getGrantedKeywords().add(Keyword.FLYING);

        Permanent groundBlocker = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent flyerBlocker = addCreatureReady(player2, new KjeldoranWarrior());
        flyerBlocker.getGrantedKeywords().add(Keyword.FLYING);

        int gAtk = gd.playerBattlefields.get(player1.getId()).indexOf(groundAttacker);
        int fAtk = gd.playerBattlefields.get(player1.getId()).indexOf(flyingAttacker);

        groundAttacker.setAttacking(true);
        flyingAttacker.setAttacking(true);
        assignBlocker(groundBlocker, groundAttacker, gAtk);
        assignBlocker(flyerBlocker, flyingAttacker, fAtk);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(groundAttacker.getId(), flyingAttacker.getId()));
        harness.passBothPriorities();

        assertThat(groundBlocker.getBlockingTargetIds()).containsExactly(groundAttacker.getId());
        assertThat(flyerBlocker.getBlockingTargetIds()).containsExactly(flyingAttacker.getId());
    }

    @Test
    @DisplayName("Does nothing if a target leaves combat before resolution")
    void noSwapWhenTargetLeavesCombat() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent attackerA = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent attackerB = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent blockerA = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent blockerB = addCreatureReady(player2, new KjeldoranWarrior());

        setupTwoBlockedAttackers(attackerA, attackerB, blockerA, blockerB);

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(attackerA.getId(), attackerB.getId()));
        blockerA.setBlocking(false);
        attackerA.setAttacking(false);
        harness.passBothPriorities();

        assertThat(blockerA.isBlocking()).isFalse();
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerB.getId());
    }

    @Test
    @DisplayName("Cannot activate outside declare blockers step")
    void cannotActivateOutsideDeclareBlockers() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent attackerA = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent attackerB = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent blockerA = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent blockerB = addCreatureReady(player2, new KjeldoranWarrior());

        setupTwoBlockedAttackers(attackerA, attackerB, blockerA, blockerB);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(attackerA.getId(), attackerB.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers");
    }

    @Test
    @DisplayName("Requires both targets to be blocked attacking creatures")
    void requiresBothTargetsToBeBlockedAttackingCreatures() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent blockedAttacker = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent unblockedAttacker = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        int blockedAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blockedAttacker);
        blockedAttacker.setAttacking(true);
        unblockedAttacker.setAttacking(true);
        assignBlocker(blocker, blockedAttacker, blockedAttackerIndex);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(blockedAttacker.getId(), unblockedAttacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocked attacking creature");
    }

    @Test
    @DisplayName("Moves a blocker onto an attacker blocked without blockers")
    void swapsWhenOneTargetIsBlockedWithoutBlockers() {
        Permanent jarkeld = addCreatureReady(player1, new GeneralJarkeld());
        Permanent attackerA = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent attackerB = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent blockerB = addCreatureReady(player2, new KjeldoranWarrior());

        int aIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerA);
        int bIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerB);

        attackerA.setAttacking(true);
        attackerA.setBlockedWithoutBlockers(true);
        attackerB.setAttacking(true);
        assignBlocker(blockerB, attackerB, bIdx);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jarkeld),
                0,
                List.of(attackerA.getId(), attackerB.getId()));
        harness.passBothPriorities();

        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(blockerB.getBlockingTargets()).containsExactly(aIdx);
        assertThat(attackerA.isBlockedWithoutBlockers()).isFalse();
        assertThat(attackerB.isBlockedWithoutBlockers()).isTrue();
    }

    private void setupTwoBlockedAttackers(
            Permanent attackerA, Permanent attackerB,
            Permanent blockerA, Permanent blockerB) {
        int aIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerA);
        int bIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerB);

        attackerA.setAttacking(true);
        attackerB.setAttacking(true);
        assignBlocker(blockerA, attackerA, aIdx);
        assignBlocker(blockerB, attackerB, bIdx);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void assignBlocker(Permanent blocker, Permanent attacker, int attackerIndex) {
        blocker.setBlocking(true);
        blocker.addBlockingTarget(attackerIndex);
        blocker.addBlockingTargetId(attacker.getId());
    }
}
