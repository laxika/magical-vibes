package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChooseYourWeapon.class, AirElemental.class, GrizzlyBears.class, Mountain.class})
class ChooseYourWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles target creature's power and toughness until end of turn")
    void doublesTargetCreaturePowerAndToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0, target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The doubling wears off at end of turn")
    void doublingWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 5 damage to target creature with flying")
    void dealsFiveDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(1, target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The doubling mode cannot target a noncreature permanent")
    void doublingModeCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> cast(0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Archery mode cannot target a creature without flying")
    void archeryModeCannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, target))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new ChooseYourWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
