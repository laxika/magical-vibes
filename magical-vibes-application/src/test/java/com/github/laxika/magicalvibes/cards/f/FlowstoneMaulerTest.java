package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowstoneMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +1/-1 until end of turn")
    void activatingAbilityBoostsSelf() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void repeatedActivationsStack() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(5);
    }
}
