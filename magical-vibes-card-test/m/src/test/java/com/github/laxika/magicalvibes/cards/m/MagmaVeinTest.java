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
    @Test
    @DisplayName("The land is sacrificed as a cost before damage resolves")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = addCreatureReady(player2, new NantukoDisciple());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(creature.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Repeated activations accumulate lethal damage without tapping Magma Vein")
    void repeatedActivationsKillGroundCreaturesButSpareFlyers() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player1, new Forest());
        Permanent groundCreature = addCreatureReady(player2, new NantukoDisciple());
        Permanent flyingCreature = addCreatureReady(player2, new AvenFlock());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nantuko Disciple");
        harness.assertNotOnBattlefield(player2, "Nantuko Disciple");
        harness.assertOnBattlefield(player2, "Aven Flock");
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Magma Vein");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
