package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WardOfPietyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to the enchanted creature is redirected to the target creature")
    void redirectsDamageToCreature() {
        Permanent enchanted = addReadyStats(player1, 3, 3);
        Permanent aura = attachWard(player1, enchanted);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage can be redirected to a player")
    void redirectsDamageToPlayer() {
        Permanent enchanted = addReadyStats(player1, 3, 3);
        Permanent aura = attachWard(player1, enchanted);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the enchanted creature")
    void redirectsOnlyOneDamage() {
        Permanent enchanted = addReadyStats(player1, 3, 3);
        Permanent aura = attachWard(player1, enchanted);
        Permanent destination = addReadyStats(player2, 3, 3);
        Permanent firstBolter = addReady(player1, new ProdigalPyromancer());
        Permanent secondBolter = addReady(player1, new ProdigalPyromancer());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, firstBolter), null, enchanted.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondBolter), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(enchanted.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent enchanted = addReadyStats(player1, 3, 3);
        Permanent aura = attachWard(player1, enchanted);
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private Permanent attachWard(Player player, Permanent enchanted) {
        Permanent aura = new Permanent(new WardOfPiety());
        aura.setAttachedTo(enchanted.getId());
        aura.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
