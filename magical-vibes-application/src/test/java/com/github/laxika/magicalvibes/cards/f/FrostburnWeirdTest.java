package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostburnWeird.class})
class FrostburnWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Hybrid ability can be paid with blue mana")
    void pumpPaidWithBlue() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Hybrid ability can be paid with red mana")
    void pumpPaidWithRed() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Pump ability stacks across multiple activations")
    void pumpStacks() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(2);
        assertThat(weird.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(0);
        assertThat(weird.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent weird = harness.addToBattlefieldAndReturn(player1, new FrostburnWeird());
        weird.setSummoningSick(true);
        weird.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);
        assertThat(weird.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the hybrid activation cost")
    void cannotPayWithColorlessMana() {
        Permanent weird = addReadyWeird();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(weird.getPowerModifier()).isZero();
        assertThat(weird.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Fourth activation puts Frostburn Weird into the graveyard for zero toughness")
    void diesWhenRepeatedActivationsReduceToughnessToZero() {
        addReadyWeird();
        harness.addMana(player1, ManaColor.BLUE, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Frostburn Weird");
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frostburn Weird");
        harness.assertInGraveyard(player1, "Frostburn Weird");
    }

    private Permanent addReadyWeird() {
        Permanent weird = harness.addToBattlefieldAndReturn(player1, new FrostburnWeird());
        weird.setSummoningSick(false);
        return weird;
    }
}
