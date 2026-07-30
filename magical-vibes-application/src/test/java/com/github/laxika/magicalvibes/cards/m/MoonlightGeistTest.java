package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoonlightGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prevents combat damage dealt to and by Moonlight Geist")
    void abilityPreventsCombatDamageBothWays() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent geist = addCreatureReady(player2, new MoonlightGeist());

        blockWithGeist();
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        assertThat(geist.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Moonlight Geist");
    }

    @Test
    @DisplayName("Without activating the ability Moonlight Geist deals and takes combat damage normally")
    void combatDamageIsNotPreventedByDefault() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new MoonlightGeist());

        blockWithGeist();
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Moonlight Geist");
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Moonlight Geist. */
    private void blockWithGeist() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
