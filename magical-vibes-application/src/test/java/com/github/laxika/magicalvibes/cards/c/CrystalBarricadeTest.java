package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrystalBarricadeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot target the controller")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Noncombat damage to another creature you control is prevented")
    void preventsNoncombatDamageToAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The source creature is not protected by its own effect")
    void doesNotPreventNoncombatDamageToSource() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new CrystalBarricade());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, barricade.getId());
        harness.passBothPriorities();

        assertThat(barricade.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to another creature you control is not prevented")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new CrystalBarricade());
        Permanent blocker = addReadyBlocker(player1);
        addReadyAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyAttacker(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private Permanent addReadyBlocker(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player.getId()).add(blocker);
        return blocker;
    }
}
