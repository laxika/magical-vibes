package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BurningPalmEfreet.class, AzimaetDrake.class, MtendaGriffin.class, JungleWurm.class, Incinerate.class})
class BurningPalmEfreetTest extends BaseCardTest {

    private void payAbility() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals 2 damage to a target flyer and strips its flying")
    void damagesAndStripsFlying() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Kills a 2/2 flyer")
    void killsSmallFlyer() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent mtendaGriffin = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        payAbility();

        harness.activateAbility(player1, 0, 0, null, mtendaGriffin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mtenda Griffin");
    }

    @Test
    @DisplayName("Flying comes back at end of turn")
    void flyingReturnsAtEndOfTurn() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can activate more than once without tapping")
    void canActivateMoreThanOnceWithoutTapping() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        Permanent mtendaGriffin = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        payAbility();
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, mtendaGriffin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mtenda Griffin");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not resolve if the target loses flying before resolution")
    void doesNotResolveIfTargetLosesFlyingBeforeResolution() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        payAbility();
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleWurm());
        payAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("Can damage and remove flying from a creature you control")
    void canTargetOwnFlyingCreature() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AzimaetDrake());
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Azimaet Drake");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability still resolves after Burning Palm Efreet dies")
    void resolvesAfterSourceDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Burning Palm Efreet");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability does not affect a different flyer after its target dies")
    void doesNotResolveAfterTargetDies() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());
        Permanent otherFlyer = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        payAbility();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Azimaet Drake");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Mtenda Griffin");
        assertThat(otherFlyer.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, otherFlyer, Keyword.FLYING)).isTrue();
    }
}
