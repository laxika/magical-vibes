package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantedBeing.class, GrizzlyBears.class, HolyStrength.class, ProdigalPyromancer.class})
class EnchantedBeingTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from an enchanted creature")
    void preventsCombatDamageFromEnchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        being.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachHolyStrength(attacker);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(being.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from an unenchanted creature")
    void doesNotPreventCombatDamageFromUnenchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        being.setBlocking(true);
        being.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(being.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from an enchanted creature")
    void doesNotPreventNoncombatDamageFromEnchantedCreature() {
        Permanent being = addCreatureReady(player2, new EnchantedBeing());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachHolyStrength(pyromancer);

        harness.activateAbility(player1, 0, null, being.getId());
        harness.passBothPriorities();

        assertThat(being.getMarkedDamage()).isEqualTo(1);
    }

    private void attachHolyStrength(Permanent creature) {
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
