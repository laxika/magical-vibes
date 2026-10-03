package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevouringSwarm.class, RuneclawBear.class})
class DevouringSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives Devouring Swarm +1/+1")
    void sacrificeBoostsSwarm() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(swarm.getEffectivePower()).isEqualTo(3);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and the boosts stack")
    void boostsStack() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent bears2 = addCreatureReady(player1, new RuneclawBear());

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
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(swarm.getEffectivePower()).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(swarm.getEffectivePower()).isEqualTo(2);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Devouring Swarm can be sacrificed to its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new DevouringSwarm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Devouring Swarm");
        harness.assertNotOnBattlefield(player1, "Devouring Swarm");
    }

    @Test
    @DisplayName("Sacrifice is paid before the boost resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent swarm = addCreatureReady(player1, new DevouringSwarm());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).hasSize(1);
        assertThat(swarm.getEffectivePower()).isEqualTo(2);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(swarm.getEffectivePower()).isEqualTo(3);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Swarm can sacrifice a tapped creature")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DevouringSwarm());
        swarm.setTapped(true);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bear.setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(swarm.isTapped()).isTrue();
        assertThat(swarm.getEffectivePower()).isEqualTo(3);
        assertThat(swarm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing the source does not boost another Devouring Swarm")
    void sacrificingSourceDoesNotBoostAnotherSwarm() {
        Permanent source = addCreatureReady(player1, new DevouringSwarm());
        Permanent other = addCreatureReady(player1, new DevouringSwarm());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Devouring Swarm");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
