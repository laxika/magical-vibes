package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtleshellChangeling.class, ImperiousPerfect.class})
class TurtleshellChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped creature can activate the switch ability")
    void canActivateWhileTapped() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new TurtleshellChangeling());
        changeling.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
        assertThat(changeling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Changeling receives an Elf bonus added after its power and toughness switch")
    void tribalBonusAppliesBeforeSwitchEvenWhenAddedLater() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new TurtleshellChangeling());
        Permanent opposingChangeling = harness.addToBattlefieldAndReturn(player2, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new ImperiousPerfect());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingChangeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingChangeling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two activations cancel each other out")
    void twoActivationsCancelEachOtherOut() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new TurtleshellChangeling());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(4);
    }
}
