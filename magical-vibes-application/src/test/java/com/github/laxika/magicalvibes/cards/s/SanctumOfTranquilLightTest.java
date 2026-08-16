package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanctumOfTranquilLightTest extends BaseCardTest {

    @Test
    @DisplayName("Shrines reduce the activation cost and the ability taps target creature")
    void shrinesReduceActivationCostAndTapCreature() {
        addReadySanctum();
        harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        addReadySanctum();
        harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addReadySanctum() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SanctumOfTranquilLight());
        sanctum.setSummoningSick(false);
    }
}
