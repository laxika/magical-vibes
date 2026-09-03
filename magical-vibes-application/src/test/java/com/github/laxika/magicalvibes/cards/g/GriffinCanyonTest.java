package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffinCanyon.class, DarajaGriffin.class, HulkingCyclops.class})
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
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
        griffin.tap();

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isFalse();
        assertThat(griffin.getPowerModifier()).isEqualTo(1);
        assertThat(griffin.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targets an untapped Griffin an opponent controls and still gives it +1/+1")
    void boostsUntappedOpponentGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new DarajaGriffin());

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
        Permanent nonGriffin = harness.addToBattlefieldAndReturn(player1, new HulkingCyclops());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonGriffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
    }
}
