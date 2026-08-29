package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeteransPowerbladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPower() {
        Permanent creature = addCreatureReady(player1, new VeteranArmorsmith());
        Permanent powerblade = addPowerbladeReady(player1);
        powerblade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void soldierEquipAttachesForWhiteMana() {
        Permanent powerblade = addPowerbladeReady(player1);
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void soldierEquipRejectsNonSoldier() {
        addPowerbladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Soldier");
    }

    @Test
    void genericEquipAttachesToNonSoldier() {
        Permanent powerblade = addPowerbladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(bears.getId());
    }

    private Permanent addPowerbladeReady(Player player) {
        Permanent perm = new Permanent(new VeteransPowerblade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
