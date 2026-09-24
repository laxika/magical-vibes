package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodthirstyBlade.class, GrizzlyBears.class})
class BloodthirstyBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and is goaded")
    void equippedCreatureGetsBoostAndGoaded() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip targets a creature an opponent controls")
    void equipAttachesToOpponentCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature you control")
    void equipRejectsOwnCreature() {
        addBladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBladeReady(Player player) {
        Permanent blade = harness.addToBattlefieldAndReturn(player, new BloodthirstyBlade());
        blade.setSummoningSick(false);
        return blade;
    }
}
