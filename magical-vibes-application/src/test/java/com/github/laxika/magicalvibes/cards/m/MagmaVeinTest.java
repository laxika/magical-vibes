package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NantukoDisciple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaVein.class, AvenFlock.class, Forest.class, NantukoDisciple.class})
class MagmaVeinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land deals 1 damage to each creature without flying")
    void sacrificingLandDealsDamageOnlyToCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player1, new Forest());
        Permanent ownGroundCreature = addCreatureReady(player1, new NantukoDisciple());
        Permanent ownFlyingCreature = addCreatureReady(player1, new AvenFlock());
        Permanent opposingGroundCreature = addCreatureReady(player2, new NantukoDisciple());
        Permanent opposingFlyingCreature = addCreatureReady(player2, new AvenFlock());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingGroundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownFlyingCreature.getMarkedDamage()).isZero();
        assertThat(opposingFlyingCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Magma Vein");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Magma Vein cannot be activated when only the opponent controls a land")
    void cannotActivateWithoutLandToSacrifice() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Magma Vein cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }
}
