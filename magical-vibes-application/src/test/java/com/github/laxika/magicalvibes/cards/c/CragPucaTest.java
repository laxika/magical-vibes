package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CragPuca.class})
class CragPucaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new CragPuca());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new CragPuca());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two activations cancel and a third switches again")
    void repeatedActivationsSwitchEachTime() {
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new CragPuca());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped newly entered Puca can activate and switches only itself")
    void tappedPucaSwitchesOnlyItself() {
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new CragPuca());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CragPuca());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CragPuca());
        puca.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
        assertThat(puca.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
    }
}
