package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PennonBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each creature its controller controls")
    void boostsForEachCreatureControlledByEquipmentController() {
        Permanent equippedCreature = addReadyCreature(player2);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pennon Blade updates its bonus as the Equipment controller's creature count changes")
    void boostUpdatesWithCreatureCount() {
        Permanent equippedCreature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);

        Permanent otherCreature = addReadyCreature(player1);
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(otherCreature);
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {4} attaches Pennon Blade to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBladeReady(Player player) {
        Permanent permanent = new Permanent(new PennonBlade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
