package com.github.laxika.magicalvibes.cards.a;

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

class ArmoredTransportTest extends BaseCardTest {

    private Permanent addAttacker(UUID controllerId, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(UUID controllerId, com.github.laxika.magicalvibes.model.Card card, int blockingTarget) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
        return blocker;
    }

    @Test
    @DisplayName("Combat damage from a blocking creature is prevented while Armored Transport attacks")
    void preventsDamageFromItsBlocker() {
        Permanent transport = addAttacker(player1.getId(), new ArmoredTransport());
        Permanent blocker = addBlocker(player2.getId(), new GrizzlyBears(), 0);
        blocker.addBlockingTargetId(transport.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The 2/2 blocker's damage would kill the 2/1, but it is prevented; the Transport still deals its 2.
        assertThat(transport.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(transport);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage from a creature Armored Transport blocks is not prevented")
    void doesNotPreventDamageFromTheCreatureItBlocks() {
        Permanent transport = addBlocker(player1.getId(), new ArmoredTransport(), 0);
        addAttacker(player2.getId(), new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Only damage from creatures blocking it is prevented, so the attacker's 2 damage kills the 2/1.
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(transport);
    }

    @Test
    @DisplayName("Noncombat damage to Armored Transport is not prevented")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player2, new ArmoredTransport());
        UUID transportId = harness.getPermanentId(player2, "Armored Transport");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, transportId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Armored Transport");
    }
}
