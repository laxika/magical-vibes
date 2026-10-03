package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowsLizard.class})
class BellowsLizardTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R}: Bellows Lizard gets +1/+0 until end of turn")
    void boostsItself() {
        Permanent lizard = addCreatureReady(player1, new BellowsLizard());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void boostsStack() {
        Permanent lizard = addCreatureReady(player1, new BellowsLizard());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent lizard = addCreatureReady(player1, new BellowsLizard());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability works while tapped and summoning sick, using any color for the generic cost")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new BellowsLizard());
        lizard.setSummoningSick(true);
        lizard.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(1);
        assertThat(lizard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the source Lizard receives the boost")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new BellowsLizard());
        Permanent ally = addCreatureReady(player1, new BellowsLizard());
        Permanent opponent = addCreatureReady(player2, new BellowsLizard());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
    }
}
