package com.github.laxika.magicalvibes.cards.b;

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

class BubbleMatrixTest extends BaseCardTest {

    private Permanent addAttacker(UUID controllerId) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(UUID controllerId, int blockingTarget) {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
        return blocker;
    }

    @Test
    @DisplayName("Noncombat damage to the controller's creature is prevented")
    void preventsNoncombatDamageToOwnCreature() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is prevented too")
    void preventsNoncombatDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(enemyBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage between creatures is prevented on both sides")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        Permanent blocker = addBlocker(player1.getId(), 0);
        Permanent attacker = addAttacker(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to players is not prevented")
    void doesNotPreventDamageToPlayers() {
        harness.addToBattlefield(player1, new BubbleMatrix());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
