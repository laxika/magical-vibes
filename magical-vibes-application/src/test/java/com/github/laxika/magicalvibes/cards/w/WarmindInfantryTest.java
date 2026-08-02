package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarmindInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives this creature +2/+0 when it attacks with two other creatures")
    void battalionBoostsSelf() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(4);
        assertThat(infantry.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithOneOtherAttacker() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Battalion boost wears off at end of turn")
    void battalionBoostWearsOff() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(infantry.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(infantry.getEffectivePower()).isEqualTo(2);
    }
}
