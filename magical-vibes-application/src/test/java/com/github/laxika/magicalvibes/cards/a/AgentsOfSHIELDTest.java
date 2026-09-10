package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentsOfSHIELD.class, GrizzlyBears.class})
class AgentsOfSHIELDTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone gives the attacker +1/+1 until end of turn")
    void attacksAloneBoostsAttacker() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(agents.getPowerModifier()).isEqualTo(1);
        assertThat(agents.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking alone boosts the other attacking creature")
    void attacksAloneBoostsOtherAttacker() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(agents.getPowerModifier()).isEqualTo(0);
        assertThat(agents.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger the boost")
    void attackingWithAnotherCreatureDoesNotBoost() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(agents.getPowerModifier()).isEqualTo(0);
        assertThat(agents.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(agents.getPowerModifier()).isEqualTo(0);
        assertThat(agents.getToughnessModifier()).isEqualTo(0);
    }
}
