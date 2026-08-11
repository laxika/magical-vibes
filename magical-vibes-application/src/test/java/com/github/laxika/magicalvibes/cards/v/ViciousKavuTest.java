package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViciousKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent viciousKavu = addCreatureReady(player1, new ViciousKavu());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(viciousKavu.getPowerModifier()).isEqualTo(2);
        assertThat(viciousKavu.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+2/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent viciousKavu = addCreatureReady(player1, new ViciousKavu());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(viciousKavu.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(viciousKavu.getPowerModifier()).isEqualTo(0);
        assertThat(viciousKavu.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost while it sits on the battlefield without attacking")
    void noBoostWithoutAttacking() {
        Permanent viciousKavu = addCreatureReady(player1, new ViciousKavu());

        assertThat(viciousKavu.getPowerModifier()).isEqualTo(0);
        assertThat(viciousKavu.getToughnessModifier()).isEqualTo(0);
    }
}
