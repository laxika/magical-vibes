package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DranaKalastriaBloodchief.class, LagacLizard.class, Forest.class})
class DranaKalastriaBloodchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Gives Drana +X/+0 and the target creature -0/-X")
    void givesDranaPowerAndTargetCreatureToughnessReduction() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power boost and toughness reduction wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DranaKalastriaBloodchief());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXLeavesBothCreaturesUnchanged() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Lagac Lizard");
    }

    @Test
    void lethalReductionStillGrantsTheFullChosenPowerBoost() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.activateAbility(player1, 0, 5, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(9);
        harness.assertNotOnBattlefield(player2, "Lagac Lizard");
        harness.assertInGraveyard(player2, "Lagac Lizard");
    }

    @Test
    void canTargetItselfAndApplyBothModifiers() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 2, drana.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(2);
    }

    @Test
    void repeatedActivationsAccumulateWithoutTappingDrana() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(drana.isTapped()).isFalse();
    }

    @Test
    void illegalTargetPreventsThePowerBoost() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.activateAbility(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lagac Lizard");
        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(7);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillReducesTargetToughnessAfterDranaLeaves() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.activateAbility(player1, 0, 4, drana.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drana, Kalastria Bloodchief");
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Lagac Lizard");
    }

    @Test
    void activationRequiresTwoBlackManaInAdditionToX() {
        harness.addToBattlefield(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
