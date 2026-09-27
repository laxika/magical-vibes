package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantedBeing.class, DAvenantArcher.class, GiantStrength.class})
class EnchantedBeingTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from an enchanted creature")
    void preventsCombatDamageFromEnchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        being.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new DAvenantArcher());
        attachGiantStrength(attacker);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(being.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage from an enchanted creature regardless of Aura controller")
    void preventsCombatDamageFromEnchantedCreatureRegardlessOfAuraController() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        being.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new DAvenantArcher());
        attachGiantStrength(player2, attacker);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(being.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from an unenchanted creature")
    void doesNotPreventCombatDamageFromUnenchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        being.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new DAvenantArcher());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(being.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from an enchanted creature")
    void doesNotPreventNoncombatDamageFromEnchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        Permanent archer = addCreatureReady(player1, new DAvenantArcher());
        attachGiantStrength(archer);

        harness.activateAbility(player1, 0, null, being.getId());
        harness.passBothPriorities();

        assertThat(being.getMarkedDamage()).isEqualTo(1);
    }

    private void attachGiantStrength(Permanent creature) {
        attachGiantStrength(player1, creature);
    }

    private void attachGiantStrength(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new GiantStrength());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }
}
