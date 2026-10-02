package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RuneCervinRider.class)
class RuneCervinRiderTest extends BaseCardTest {

    @Test
    @DisplayName("{G/W}{G/W} paid with white gives +1/+1")
    void boostPaidWithWhite() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        int basePower = gqs.getEffectivePower(gd, rider);
        int baseToughness = gqs.getEffectiveToughness(gd, rider);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("{G/W}{G/W} can be paid with green mana too")
    void boostPaidWithGreen() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        int basePower = gqs.getEffectivePower(gd, rider);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("{G/W}{G/W} can use one green and one white mana")
    void boostPaidWithMixedHybridMana() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        int basePower = gqs.getEffectivePower(gd, rider);
        int baseToughness = gqs.getEffectiveToughness(gd, rider);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The activation requires two hybrid mana of the right colors")
    void cannotActivateWithoutTwoHybridMana() {
        harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation cannot be paid with unrelated colors")
    void cannotActivateWithUnrelatedMana() {
        harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new RuneCervinRider());
        int basePower = gqs.getEffectivePower(gd, rider);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(basePower);
    }
}
