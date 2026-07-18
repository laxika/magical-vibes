package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

class UncleIstvanTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature is prevented")
    void combatDamageFromCreatureIsPrevented() {
        Permanent istvan = addCreatureReady(player1, new UncleIstvan());
        istvan.setBlocking(true);
        istvan.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Air Elemental (4/4) would be lethal to a 1/3, but all combat damage from the creature is prevented.
        harness.assertOnBattlefield(player1, "Uncle Istvan");
        assertThat(istvan.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a creature source is prevented")
    void noncombatCreatureSourceDamageIsPrevented() {
        Permanent istvan = addCreatureReady(player2, new UncleIstvan());
        addCreatureReady(player1, new ProdigalPyromancer());
        UUID istvanId = harness.getPermanentId(player2, "Uncle Istvan");

        harness.activateAbility(player1, 0, null, istvanId);
        harness.passBothPriorities();

        assertThat(istvan.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Uncle Istvan");
    }

    @Test
    @DisplayName("Damage from a noncreature source (a spell) is not prevented")
    void spellSourceDamageIsNotPrevented() {
        Permanent istvan = addCreatureReady(player2, new UncleIstvan());
        UUID istvanId = harness.getPermanentId(player2, "Uncle Istvan");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, istvanId);
        harness.passBothPriorities();

        // Shock is an instant (a noncreature source), so its 2 damage lands normally.
        assertThat(istvan.getMarkedDamage()).isEqualTo(2);
    }
}
