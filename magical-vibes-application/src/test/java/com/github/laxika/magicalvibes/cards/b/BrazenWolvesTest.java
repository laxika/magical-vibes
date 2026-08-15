package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrazenWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Brazen Wolves +2/+0 until end of turn")
    void attackTriggerBoostsPower() {
        Permanent wolves = addCreatureReady(player1, new BrazenWolves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(wolves.getPowerModifier()).isEqualTo(2);
        assertThat(wolves.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOff() {
        Permanent wolves = addCreatureReady(player1, new BrazenWolves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(wolves.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolves.getPowerModifier()).isZero();
        assertThat(wolves.getToughnessModifier()).isZero();
    }
}
