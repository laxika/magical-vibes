package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DauntlessVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Dauntless Veteran boosts creatures you control")
    void attackBoostsControlledCreatures() {
        addCreatureReady(player1, new DauntlessVeteran());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauntless Veteran boosts itself when it attacks")
    void attackBoostsItself() {
        Permanent veteran = addCreatureReady(player1, new DauntlessVeteran());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauntless Veteran does not boost an opponent's creatures")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new DauntlessVeteran());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(opponent.getPowerModifier()).isEqualTo(0);
        assertThat(opponent.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Dauntless Veteran's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DauntlessVeteran());
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
