package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FlowstoneHellion.class)
class FlowstoneHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +1/-1 until end of turn")
    void activatingAbilityBoostsSelf() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void repeatedActivationsStack() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three activations reduce its toughness to zero and it dies")
    void threeActivationsCauseStateBasedDeath() {
        addCreatureReady(player1, new FlowstoneHellion());

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Flowstone Hellion");
        harness.assertInGraveyard(player1, "Flowstone Hellion");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(3);
    }
}
