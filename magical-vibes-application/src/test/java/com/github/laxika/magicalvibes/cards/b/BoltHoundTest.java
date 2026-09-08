package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoltHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+0 when Bolt Hound attacks")
    void otherCreaturesGetBoost() {
        addCreatureReady(player1, new BoltHound());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bolt Hound does not boost itself")
    void doesNotBoostItself() {
        Permanent hound = addCreatureReady(player1, new BoltHound());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hound.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new BoltHound());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enemy.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BoltHound());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }
}
