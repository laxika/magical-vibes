package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValakutFireboarTest extends BaseCardTest {

    @Test
    @DisplayName("Switches power and toughness when it attacks")
    void switchesPowerAndToughnessWhenAttacking() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(1);
    }

    @Test
    @DisplayName("The switch wears off at end of turn")
    void switchWearsOffAtEndOfTurn() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(7);
    }
}
