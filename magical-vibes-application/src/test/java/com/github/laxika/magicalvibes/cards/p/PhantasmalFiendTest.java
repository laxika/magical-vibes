package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalFiend.class})
class PhantasmalFiendTest extends BaseCardTest {

    @Test
    @DisplayName("{B} gives +1/-1 until end of turn")
    void boostsSelf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new PhantasmalFiend());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(5);
    }

    @Test
    @DisplayName("{1}{U} switches power and toughness")
    void switchesPowerAndToughness() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new PhantasmalFiend());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost applies before the switch, so the switched values include it (CR 613.4d)")
    void boostThenSwitch() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new PhantasmalFiend());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switch wears off at the end of the turn")
    void switchWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new PhantasmalFiend());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(5);
    }
}
