package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TatteredApparition.class)
class TatteredApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability gives Tattered Apparition +1/+1 until end of turn")
    void boostsSelf() {
        Permanent apparition = addApparition();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(3);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability can be used repeatedly if enough mana is available")
    void boostsSelfRepeatedly() {
        Permanent apparition = addApparition();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(4);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent apparition = addApparition();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(apparition.getPowerModifier()).isEqualTo(0);
        assertThat(apparition.getToughnessModifier()).isEqualTo(0);
        assertThat(apparition.getEffectivePower()).isEqualTo(2);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addApparition() {
        return harness.addToBattlefieldAndReturn(player1, new TatteredApparition());
    }
}
