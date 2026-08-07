package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class BenalishMissionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the blocked attacker's combat damage, so the blocker survives")
    void preventsBlockedAttackerCombatDamage() {
        addCreatureReady(player2, new BenalishMissionary());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, battlefieldIndex(player2, "Benalish Missionary"), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat(player1);

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Cannot target an unblocked attacker")
    void cannotTargetUnblockedAttacker() {
        addCreatureReady(player2, new BenalishMissionary());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);

        int index = battlefieldIndex(player2, "Benalish Missionary");
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, index, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
