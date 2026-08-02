package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BorderlandMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Borderland Marauder +2/+0")
    void attackTriggerBoostsPower() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(marauder.getPowerModifier()).isEqualTo(2);
        assertThat(marauder.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, marauder)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(marauder.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("No boost while Borderland Marauder stays back")
    void noBoostWithoutAttacking() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(marauder.getPowerModifier()).isZero();
    }
}
