package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, agents)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, agents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking alone boosts the other attacking creature")
    void attacksAloneBoostsOtherAttacker() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, agents)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, agents)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger the boost")
    void attackingWithAnotherCreatureDoesNotBoost() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, agents)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, agents)).isEqualTo(0);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(0);
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

        assertThat(PermanentGrants.powerBoostOf(gd, agents)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, agents)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each copy boosts the lone attacker")
    void multipleCopiesBoostTheLoneAttacker() {
        Permanent attacker = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent otherAgents = addCreatureReady(player1, new AgentsOfSHIELD());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, otherAgents)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherAgents)).isZero();
    }

    @Test
    @DisplayName("An opponent's lone attacker does not receive the boost")
    void opponentAttackingAloneDoesNotTrigger() {
        addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("The boost resolves even after Agents leaves the battlefield")
    void removingSourceDoesNotStopTheBoost() {
        Permanent agents = addCreatureReady(player1, new AgentsOfSHIELD());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, agents));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
    }
}
