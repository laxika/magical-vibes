package com.github.laxika.magicalvibes.cards.c;

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

class ChampionLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature is prevented")
    void combatDamageFromCreatureIsPrevented() {
        Permanent lancer = addCreatureReady(player1, new ChampionLancer());
        lancer.setBlocking(true);
        lancer.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Champion Lancer");
        assertThat(lancer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a creature source is prevented")
    void noncombatCreatureSourceDamageIsPrevented() {
        Permanent lancer = addCreatureReady(player2, new ChampionLancer());
        addCreatureReady(player1, new ProdigalPyromancer());
        UUID lancerId = harness.getPermanentId(player2, "Champion Lancer");

        harness.activateAbility(player1, 0, null, lancerId);
        harness.passBothPriorities();

        assertThat(lancer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Champion Lancer");
    }

    @Test
    @DisplayName("Damage from a noncreature source is not prevented")
    void noncreatureSourceDamageIsNotPrevented() {
        Permanent lancer = addCreatureReady(player2, new ChampionLancer());
        UUID lancerId = harness.getPermanentId(player2, "Champion Lancer");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, lancerId);
        harness.passBothPriorities();

        assertThat(lancer.getMarkedDamage()).isEqualTo(2);
    }
}
