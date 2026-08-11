package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PiannaNomadCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Pianna gives all your attacking creatures +1/+1")
    void boostsAllOwnAttackers() {
        Permanent pianna = addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(pianna.getPowerModifier()).isEqualTo(1);
        assertThat(pianna.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pianna does not boost creatures that are not attacking")
    void doesNotBoostNonAttackers() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pianna does not boost an opponent's attacking creatures")
    void doesNotBoostOpponentsAttackers() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(opponentBears.getPowerModifier()).isZero();
        assertThat(opponentBears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pianna's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PiannaNomadCaptain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }
}
