package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwordOfKaldraTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches Sword of Kaldra to a creature you control")
    void equipAttachesToCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +5/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Whenever equipped creature deals damage to a creature, that creature is exiled")
    void damagedCreatureIsExiled() {
        Permanent sword = addSwordReady(player1);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        sword.setAttachedTo(pyromancer.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("The damage trigger does nothing if the damaged creature is no longer on the battlefield")
    void damagedCreatureMustStillBeOnBattlefield() {
        Permanent sword = addSwordReady(player1);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        sword.setAttachedTo(pyromancer.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new SwordOfKaldra());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }
}
