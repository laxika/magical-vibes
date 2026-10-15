package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonlightGeist.class, PillarOfFlame.class})
class MoonlightGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prevents combat damage dealt to and by Moonlight Geist")
    void abilityPreventsCombatDamageBothWays() {
        Permanent attacker = addCreatureReady(player1, new MoonlightGeist());
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
        Permanent attacker = addCreatureReady(player1, new MoonlightGeist());
        addCreatureReady(player2, new MoonlightGeist());

        blockWithGeist();
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Moonlight Geist");
    }

    @Test
    @DisplayName("Activated prevention stops damage to a player only from that Geist")
    void preventsUnblockedDamageOnlyFromActivatedGeist() {
        addCreatureReady(player1, new MoonlightGeist());
        addCreatureReady(player1, new MoonlightGeist());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Activated prevention does not prevent noncombat damage")
    void noncombatDamageStillKillsGeist() {
        Permanent geist = addCreatureReady(player1, new MoonlightGeist());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, geist.getId());

        harness.assertNotOnBattlefield(player1, "Moonlight Geist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Moonlight Geist"));
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Moonlight Geist. */
    private void blockWithGeist() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
