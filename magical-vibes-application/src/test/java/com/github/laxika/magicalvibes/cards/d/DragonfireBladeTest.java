package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BantCharm;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonfireBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and hexproof from monocolored")
    void equippedCreatureGetsBoostAndMonocoloredHexproof() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = addDragonfireBladeReady(player2);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasHexproofFromMonocolored(gd, creature)).isTrue();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multicolored spells can target the equipped creature")
    void multicoloredSpellsCanTargetEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = addDragonfireBladeReady(player2);
        blade.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new BantCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip costs {3} for a one-color creature")
    void equipCostsThreeForOneColorCreature() {
        Permanent blade = addDragonfireBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip costs {2} for a two-color creature")
    void equipCostsTwoForTwoColorCreature() {
        Permanent blade = addDragonfireBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new QasaliAmbusher());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip costs {4} for a colorless creature")
    void equipCostsFourForColorlessCreature() {
        Permanent blade = addDragonfireBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addDragonfireBladeReady(Player player) {
        Permanent permanent = new Permanent(new DragonfireBlade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
