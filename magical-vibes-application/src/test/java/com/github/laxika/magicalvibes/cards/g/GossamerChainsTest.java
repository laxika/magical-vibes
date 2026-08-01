package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GossamerChainsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand as a cost and prevents combat damage from the unblocked attacker")
    void bouncesAndPreventsUnblockedCombatDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        harness.activateAbility(player2, chainsIndex, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Gossamer Chains"));
        assertThat(countPermanents(player2, "Gossamer Chains")).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat(player1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a blocked attacker")
    void cannotTargetBlockedAttacker() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent blocked = addUnblockedAttacker(player1, player2);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(blocked.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        UUID targetId = blocked.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, chainsIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacker before blockers are declared")
    void cannotTargetBeforeBlockersDeclared() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, chainsIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addUnblockedAttacker(Player owner, Player defender) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
