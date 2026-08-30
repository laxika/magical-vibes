package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavineRaider.class})
class RavineRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Ravine Raider +1/+1")
    void activatingAbilityBoostsPowerAndToughness() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(1);
        assertThat(raider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated multiple times")
    void canActivateMultipleTimes() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(raider.getPowerModifier()).isEqualTo(2);
        assertThat(raider.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(raider.getPowerModifier()).isEqualTo(1);
        assertThat(raider.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(0);
        assertThat(raider.getToughnessModifier()).isEqualTo(0);
    }
}
