package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrAlinTheLionsClaw.class, GrizzlyBears.class})
class SyrAlinTheLionsClawTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other creatures you control until end of turn")
    void attackingBoostsOtherOwnCreatures() {
        Permanent syrAlin = addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(syrAlin.getPowerModifier()).isZero();
        assertThat(syrAlin.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger does not boost an opponent's creature")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
