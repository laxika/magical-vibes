package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TajicBladeOfTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives Tajic +5/+5 when he attacks with two other creatures")
    void battalionBoostsTajic() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isEqualTo(5);
        assertThat(tajic.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithOneOtherAttacker() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isEqualTo(0);
        assertThat(tajic.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(tajic.getPowerModifier()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tajic.getPowerModifier()).isEqualTo(0);
        assertThat(tajic.getToughnessModifier()).isEqualTo(0);
    }
}
