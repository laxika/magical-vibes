package com.github.laxika.magicalvibes.cards.d;

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

class DolmenGateTest extends BaseCardTest {

    private Permanent bearsOf(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

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
    @DisplayName("Combat damage to an attacking creature you control is prevented")
    void preventsCombatDamageToYourAttacker() {
        harness.addToBattlefield(player1, new DolmenGate());
        addBlocker(player2.getId(), 0);
        Permanent attacker = addAttacker(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Attacker takes no combat damage from its blocker — survives with no marked damage.
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature you control is not prevented (only attackers)")
    void doesNotPreventDamageToYourBlocker() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent blocker = addBlocker(player1.getId(), 0);
        addAttacker(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The blocker is not attacking, so it receives the attacker's 2 combat damage normally.
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to your attacking creature is not prevented")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent attacker = addAttacker(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Shock is noncombat damage — Dolmen Gate does not prevent it.
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }
}
