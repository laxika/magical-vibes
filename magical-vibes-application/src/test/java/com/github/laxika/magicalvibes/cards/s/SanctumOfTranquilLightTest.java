package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumOfTranquilLight.class, SanctumOfCalmWaters.class, SanctumOfStoneFangs.class,
        SanctumOfShatteredHeights.class, SanctumOfFruitfulHarvest.class, SanctumOfAll.class,
        DrowsingTyrannodon.class, Forest.class})
class SanctumOfTranquilLightTest extends BaseCardTest {

    @Test
    @DisplayName("Shrines reduce the activation cost and the ability taps target creature")
    void shrinesReduceActivationCostAndTapCreature() {
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The source Shrine counts toward its own cost reduction")
    void sourceCountsTowardReduction() {
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opposing Shrines do not reduce the activation cost")
    void opposingShrinesDoNotReduceCost() {
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player2, new SanctumOfCalmWaters());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Six Shrines reduce the cost to one white mana")
    void excessReductionStopsAtWhiteMana() {
        addSixShrines();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Even six Shrines cannot remove the white mana requirement")
    void whiteManaIsStillRequired() {
        addSixShrines();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("A newly entered, tapped Sanctum can activate repeatedly and target your creature")
    void canActivateRepeatedlyWithoutTappingSource() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SanctumOfTranquilLight());
        sanctum.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(sanctum.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addSixShrines() {
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player1, new SanctumOfAll());
    }
}
