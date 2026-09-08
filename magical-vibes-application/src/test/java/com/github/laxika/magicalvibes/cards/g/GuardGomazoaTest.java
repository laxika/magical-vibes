package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

class GuardGomazoaTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Guard Gomazoa is prevented")
    void combatDamageToGuardGomazoaIsPrevented() {
        Permanent gomazoa = addCreatureReady(player1, new GuardGomazoa());
        gomazoa.setBlocking(true);
        gomazoa.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guard Gomazoa");
        assertThat(gomazoa.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Guard Gomazoa still deals its own combat damage")
    void guardGomazoaStillDealsCombatDamage() {
        Permanent gomazoa = addCreatureReady(player1, new GuardGomazoa());
        gomazoa.setBlocking(true);
        gomazoa.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncombat damage to Guard Gomazoa is not prevented")
    void noncombatDamageToGuardGomazoaIsNotPrevented() {
        Permanent gomazoa = addCreatureReady(player2, new GuardGomazoa());
        UUID gomazoaId = harness.getPermanentId(player2, "Guard Gomazoa");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, gomazoaId);
        harness.passBothPriorities();

        assertThat(gomazoa.getMarkedDamage()).isEqualTo(2);
    }
}
