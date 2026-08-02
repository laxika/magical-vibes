package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EtherealHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures")
    void preventsCombatDamageFromCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        castEtherealHaze();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents noncombat damage from creatures")
    void preventsNoncombatDamageFromCreatures() {
        harness.setLife(player1, 20);
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        castEtherealHaze();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventNoncreatureDamage() {
        harness.setLife(player1, 20);
        castEtherealHaze();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void castEtherealHaze() {
        harness.setHand(player1, List.of(new EtherealHaze()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
