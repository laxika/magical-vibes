package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningPalmEfreet.class, AzimaetDrake.class, MtendaGriffin.class, JungleWurm.class})
class BurningPalmEfreetTest extends BaseCardTest {

    private void payAbility() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals 2 damage to a target flyer and strips its flying")
    void damagesAndStripsFlying() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new AzimaetDrake());
        payAbility();

        Permanent target = findPermanent(player2, "Azimaet Drake");
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
        harness.addToBattlefield(player2, new MtendaGriffin());
        payAbility();

        Permanent target = findPermanent(player2, "Mtenda Griffin");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mtenda Griffin");
    }

    @Test
    @DisplayName("Flying comes back at end of turn")
    void flyingReturnsAtEndOfTurn() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new AzimaetDrake());
        payAbility();

        Permanent target = findPermanent(player2, "Azimaet Drake");
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
        harness.addToBattlefield(player2, new AzimaetDrake());
        harness.addToBattlefield(player2, new MtendaGriffin());
        payAbility();
        payAbility();

        Permanent azimaetDrake = findPermanent(player2, "Azimaet Drake");
        Permanent mtendaGriffin = findPermanent(player2, "Mtenda Griffin");
        harness.activateAbility(player1, 0, 0, null, azimaetDrake.getId());
        harness.activateAbility(player1, 0, 0, null, mtendaGriffin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mtenda Griffin");
        assertThat(azimaetDrake.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, azimaetDrake, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not resolve if the target loses flying before resolution")
    void doesNotResolveIfTargetLosesFlyingBeforeResolution() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new AzimaetDrake());
        payAbility();
        payAbility();

        Permanent target = findPermanent(player2, "Azimaet Drake");
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
        harness.addToBattlefield(player2, new JungleWurm());
        payAbility();

        Permanent target = findPermanent(player2, "Jungle Wurm");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}
