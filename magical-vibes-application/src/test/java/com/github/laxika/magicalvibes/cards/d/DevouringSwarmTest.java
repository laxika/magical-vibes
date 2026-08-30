package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevouringSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives Devouring Swarm +1/+1")
    void sacrificeBoostsSwarm() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(swarm.getEffectivePower()).isEqualTo(3);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and the boosts stack")
    void boostsStack() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent bears2 = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears2.getId());
        harness.passBothPriorities();

        assertThat(swarm.getEffectivePower()).isEqualTo(4);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(swarm.getEffectivePower()).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(swarm.getEffectivePower()).isEqualTo(2);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Devouring Swarm can be sacrificed to its own ability")
    void canSacrificeItself() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Devouring Swarm");
        harness.assertNotOnBattlefield(player1, "Devouring Swarm");
    }
}
