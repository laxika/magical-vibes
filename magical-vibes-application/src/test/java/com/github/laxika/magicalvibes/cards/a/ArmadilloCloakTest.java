package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ArmadilloCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCloak(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Aura controller gains life from combat damage, including when the creature dies")
    void auraControllerGainsLifeFromCombatDamageWhenCreatureDies() {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(1);
        Permanent creature = addCreatureReady(player1, card);
        creature.setAttacking(true);
        attachCloak(player1, creature);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(8);
        blockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 10);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Armadillo Cloak");
    }

    @Test
    @DisplayName("Aura controller gains life from noncombat damage to a player")
    void auraControllerGainsLifeFromNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new CinderPyromancer());
        attachCloak(player2, creature);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void attachCloak(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new ArmadilloCloak());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
