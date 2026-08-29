package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorrowsPath.class, HillGiant.class})
class SorrowsPathTest extends BaseCardTest {

    @Test
    @DisplayName("Swaps the creatures blocked by two target blockers")
    void swapsBlockingAssignments() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new HillGiant());
        Permanent attackerB = addCreatureReady(player1, new HillGiant());
        Permanent blockerA = addCreatureReady(player2, new HillGiant());
        Permanent blockerB = addCreatureReady(player2, new HillGiant());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);

        activatePath(path, blockerA, blockerB);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(path.isTapped()).isTrue();
        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(attackerA.getMarkedDamage()).isEqualTo(2);
        assertThat(attackerB.getMarkedDamage()).isEqualTo(2);
        assertThat(blockerA.getMarkedDamage()).isZero();
        assertThat(blockerB.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not swap when a blocker cannot block the other's attacker")
    void doesNotSwapWhenBlockLegalityFails() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent groundAttacker = addCreatureReady(player1, new HillGiant());
        Permanent flyingAttacker = addCreatureReady(player1, new HillGiant());
        flyingAttacker.getGrantedKeywords().add(Keyword.FLYING);
        Permanent blockerA = addCreatureReady(player2, new HillGiant());
        Permanent blockerB = addCreatureReady(player2, new HillGiant());
        setupCombat(groundAttacker, flyingAttacker);
        assignBlocker(blockerA, groundAttacker);
        assignBlocker(blockerB, flyingAttacker);

        activatePath(path, blockerA, blockerB);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(groundAttacker.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(flyingAttacker.getId());
    }

    @Test
    @DisplayName("Does not swap when a blocker cannot block the entire opposing group")
    void doesNotSwapWhenBlockCapacityFails() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new HillGiant());
        Permanent attackerB = addCreatureReady(player1, new HillGiant());
        Permanent attackerC = addCreatureReady(player1, new HillGiant());
        Permanent blockerA = addCreatureReady(player2, new HillGiant());
        Permanent blockerB = addCreatureReady(player2, new HillGiant());
        setupCombat(attackerA, attackerB, attackerC);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerA, attackerB);
        assignBlocker(blockerB, attackerC);

        activatePath(path, blockerA, blockerB);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerA.getId(), attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerC.getId());
    }

    private void activatePath(Permanent path, Permanent blockerA, Permanent blockerB) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(path),
                0,
                List.of(blockerA.getId(), blockerB.getId()));
    }

    private void setupCombat(Permanent... attackers) {
        for (Permanent attacker : attackers) {
            attacker.setAttacking(true);
        }
    }

    private void assignBlocker(Permanent blocker, Permanent attacker) {
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        blocker.addBlockingTargetId(attacker.getId());
    }
}
