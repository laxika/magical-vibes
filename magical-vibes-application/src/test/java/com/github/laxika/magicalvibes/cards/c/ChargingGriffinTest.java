package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(griffin.getPowerModifier()).isEqualTo(1);
        assertThat(griffin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(griffin.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when it does not attack")
    void noBoostWithoutAttacking() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
    }
}
