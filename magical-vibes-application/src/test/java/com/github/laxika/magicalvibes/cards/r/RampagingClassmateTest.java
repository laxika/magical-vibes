package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RampagingClassmate.class)
class RampagingClassmateTest extends BaseCardTest {

    @Test
    @DisplayName("Gets no boost when attacking alone")
    void noBoostWhenAttackingAlone() {
        Permanent classmate = addCreatureReady(player1, new RampagingClassmate());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(classmate.getPowerModifier()).isZero();
        assertThat(classmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +1/+0 for one other attacking creature")
    void boostWithOneOtherAttackingCreature() {
        Permanent classmate = addCreatureReady(player1, new RampagingClassmate());
        addCreatureReady(player1, new RampagingClassmate());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(classmate.getPowerModifier()).isEqualTo(1);
        assertThat(classmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only attacking creatures")
    void countsOnlyAttackingCreatures() {
        Permanent classmate = addCreatureReady(player1, new RampagingClassmate());
        addCreatureReady(player1, new RampagingClassmate());
        addCreatureReady(player1, new RampagingClassmate());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(classmate.getPowerModifier()).isEqualTo(1);
        assertThat(classmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts an attacking creature controlled by another player")
    void countsAttackingCreatureControlledByAnotherPlayer() {
        Permanent classmate = addCreatureReady(player1, new RampagingClassmate());
        Permanent opponentClassmate = addCreatureReady(player2, new RampagingClassmate());
        opponentClassmate.setAttacking(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(classmate.getPowerModifier()).isEqualTo(1);
        assertThat(classmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent classmate = addCreatureReady(player1, new RampagingClassmate());
        addCreatureReady(player1, new RampagingClassmate());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(classmate.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(classmate.getPowerModifier()).isZero();
        assertThat(classmate.getToughnessModifier()).isZero();
    }
}
