package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredTransport.class, GrizzlyBears.class, Shock.class, TurnToFrog.class})
class ArmoredTransportTest extends BaseCardTest {

    private Permanent addAttacker(Player controller, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(controller, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, Card card, int blockingTarget) {
        Permanent blocker = harness.addToBattlefieldAndReturn(controller, card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        return blocker;
    }

    @Test
    @DisplayName("Combat damage from a blocking creature is prevented while Armored Transport attacks")
    void preventsDamageFromItsBlocker() {
        Permanent transport = addAttacker(player1, new ArmoredTransport());
        Permanent blocker = addBlocker(player2, new GrizzlyBears(), 0);
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
        Permanent transport = addBlocker(player1, new ArmoredTransport(), 0);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        transport.addBlockingTargetId(attacker.getId());

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
        Permanent transport = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, transport.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Armored Transport");
    }

    @Test
    @DisplayName("Losing all abilities removes protection from blocking creatures")
    void doesNotPreventBlockerDamageAfterLosingAbilities() {
        Permanent transport = addAttacker(player1, new ArmoredTransport());
        Permanent blocker = addBlocker(player2, new ArmoredTransport(), 0);
        blocker.addBlockingTargetId(transport.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, transport.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(transport);
        harness.assertInGraveyard(player1, "Armored Transport");
    }
}
