package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneralJarkeldTest extends BaseCardTest {

    @Test
    @DisplayName("Swaps exclusive blockers between two blocked attackers")
    void swapsExclusiveBlockers() {
        Permanent jarkeld = addReady(player1, new GeneralJarkeld());
        Permanent attackerA = addReady(player1, new SavannahLions());
        Permanent attackerB = addReady(player1, new SavannahLions());
        Permanent blockerA = addReady(player2, new SavannahLions());
        Permanent blockerB = addReady(player2, new SavannahLions());

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
        assertThat(blockerA.isBlocking()).isTrue();
        assertThat(blockerB.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shared blockers stay put; only exclusive blockers swap")
    void sharedBlockersRemain() {
        Permanent jarkeld = addReady(player1, new GeneralJarkeld());
        Permanent attackerA = addReady(player1, new SavannahLions());
        Permanent attackerB = addReady(player1, new SavannahLions());
        Permanent exclusiveA = addReady(player2, new SavannahLions());
        Permanent exclusiveB = addReady(player2, new SavannahLions());
        Permanent shared = addReady(player2, new SavannahLions());

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
        Permanent jarkeld = addReady(player1, new GeneralJarkeld());
        Permanent groundAttacker = addReady(player1, new SavannahLions());
        Permanent flyingAttacker = addReady(player1, new SavannahLions());
        flyingAttacker.getGrantedKeywords().add(Keyword.FLYING);

        Permanent groundBlocker = addReady(player2, new SavannahLions());
        Permanent flyerBlocker = addReady(player2, new SavannahLions());
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
    @DisplayName("Cannot activate outside declare blockers step")
    void cannotActivateOutsideDeclareBlockers() {
        Permanent jarkeld = addReady(player1, new GeneralJarkeld());
        Permanent attackerA = addReady(player1, new SavannahLions());
        Permanent attackerB = addReady(player1, new SavannahLions());
        Permanent blockerA = addReady(player2, new SavannahLions());
        Permanent blockerB = addReady(player2, new SavannahLions());

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

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
