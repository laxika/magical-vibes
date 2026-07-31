package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GracefulCatTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(1);
        assertThat(cat.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cat.getPowerModifier()).isEqualTo(0);
        assertThat(cat.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when it does not attack")
    void noBoostWithoutAttacking() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(0);
        assertThat(cat.getToughnessModifier()).isEqualTo(0);
    }
}
