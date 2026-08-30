package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BalduvianWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("removes a blocker, unblocks its former attacker, and reassigns it")
    void removesAndReassignsBlocker() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent chosenAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());

        setUpCombat(formerAttacker, chosenAttacker, blocker);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(warlord), null,
                blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();

        harness.handlePermanentChosen(player2, chosenAttacker.getId());

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(chosenAttacker.getId());
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(warlord.isTapped()).isTrue();
    }

    @Test
    @DisplayName("keeps a former attacker blocked when another blocker blocked it")
    void keepsFormerAttackerBlockedWithAnotherBlocker() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent chosenAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        Permanent otherBlocker = addCreatureReady(player2, new SavannahLions());

        setUpCombat(formerAttacker, chosenAttacker, blocker);
        int formerAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(formerAttacker);
        otherBlocker.setBlocking(true);
        otherBlocker.addBlockingTarget(formerAttackerIndex);
        otherBlocker.addBlockingTargetId(formerAttacker.getId());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(warlord), null,
                blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosenAttacker.getId());

        assertThat(otherBlocker.getBlockingTargetIds()).containsExactly(formerAttacker.getId());
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(chosenAttacker.getId());
    }

    @Test
    @DisplayName("does not reassign the blocker when no legal attacker remains")
    void doesNotReassignWhenNoAttackerCanBeBlocked() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());

        setUpCombat(formerAttacker, blocker);
        formerAttacker.getGrantedKeywords().add(Keyword.FLYING);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(warlord), null,
                blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
    }

    private void setUpCombat(Permanent formerAttacker, Permanent chosenAttacker, Permanent blocker) {
        formerAttacker.setAttacking(true);
        formerAttacker.setAttackTarget(player2.getId());
        chosenAttacker.setAttacking(true);
        chosenAttacker.setAttackTarget(player2.getId());

        int formerAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(formerAttacker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(formerAttackerIndex);
        blocker.addBlockingTargetId(formerAttacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void setUpCombat(Permanent formerAttacker, Permanent blocker) {
        formerAttacker.setAttacking(true);
        formerAttacker.setAttackTarget(player2.getId());

        int formerAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(formerAttacker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(formerAttackerIndex);
        blocker.addBlockingTargetId(formerAttacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
