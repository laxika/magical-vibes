package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CelestialMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMantle(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Combat damage doubles the enchanted creature controller's life total")
    void combatDamageDoublesEnchantedCreatureControllerLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMantle(player2, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger Celestial Mantle")
    void blockedCreatureDoesNotTrigger() {
        harness.setLife(player1, 14);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMantle(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Celestial Mantle's boost ends when the Aura leaves the battlefield")
    void boostEndsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = attachMantle(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(mantle);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent attachMantle(Player controller, Permanent creature) {
        Permanent mantle = new Permanent(new CelestialMantle());
        mantle.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(mantle);
        return mantle;
    }
}
