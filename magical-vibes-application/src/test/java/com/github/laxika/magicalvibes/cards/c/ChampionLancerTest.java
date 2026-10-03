package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionLancer.class, CrawWurm.class, ProdigalPyromancer.class, Shock.class})
class ChampionLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature is prevented")
    void combatDamageFromCreatureIsPrevented() {
        Permanent lancer = addCreatureReady(player1, new ChampionLancer());
        lancer.setBlocking(true);
        lancer.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new CrawWurm());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Champion Lancer");
        assertThat(lancer.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
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

        harness.castAndResolveInstant(player1, 0, lancerId);

        assertThat(lancer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature damage is still prevented after its source leaves the battlefield")
    void creatureDamageIsPreventedAfterSourceLeavesBattlefield() {
        Permanent lancer = addCreatureReady(player2, new ChampionLancer());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, lancer.getId());
        harness.castAndResolveInstant(player1, 0, pyromancer.getId());
        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Champion Lancer");
        assertThat(lancer.getMarkedDamage()).isZero();
    }
}
