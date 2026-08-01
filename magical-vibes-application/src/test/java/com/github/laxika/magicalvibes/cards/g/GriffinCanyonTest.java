package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GriffinCanyonTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new GriffinCanyon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps target Griffin and gives it +1/+1 until end of turn")
    void untapsAndBoostsGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new EkunduGriffin());
        griffin.tap();

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isFalse();
        assertThat(griffin.getPowerModifier()).isEqualTo(1);
        assertThat(griffin.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Griffin creature")
    void cannotTargetNonGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new EkunduGriffin());

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
    }
}
