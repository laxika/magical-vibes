package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarkOfAsylumTest extends BaseCardTest {

    private Permanent addAttacker(UUID controllerId) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(UUID controllerId, int blockingTarget) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
        return blocker;
    }

    @Test
    @DisplayName("Noncombat damage to a creature you control is prevented")
    void preventsNoncombatDamageToYourCreature() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        // Shock is noncombat damage — Mark of Asylum prevents it entirely.
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is not prevented")
    void doesNotPreventDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enemyBears.getId());
        harness.passBothPriorities();

        // Mark of Asylum only protects the controller's own creatures.
        assertThat(enemyBears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a creature you control is not prevented")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent blocker = addBlocker(player1.getId(), 0);
        addAttacker(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Combat damage is unaffected — the blocker takes the attacker's 2 damage.
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }
}
