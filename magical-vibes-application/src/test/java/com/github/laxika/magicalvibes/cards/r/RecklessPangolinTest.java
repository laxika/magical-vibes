package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RecklessPangolin.class)
class RecklessPangolinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(pangolin.getPowerModifier()).isEqualTo(1);
        assertThat(pangolin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the boost without attacking")
    void noBoostWithoutAttacking() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        assertThat(pangolin.getPowerModifier()).isZero();
        assertThat(pangolin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(pangolin.getPowerModifier()).isEqualTo(1);
        assertThat(pangolin.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pangolin.getPowerModifier()).isZero();
        assertThat(pangolin.getToughnessModifier()).isZero();
    }
}
