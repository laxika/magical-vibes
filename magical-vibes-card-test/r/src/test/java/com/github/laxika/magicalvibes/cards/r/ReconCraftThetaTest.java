package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReconCraftTheta.class, GrizzlyBears.class, CloudfinRaptor.class})
class ReconCraftThetaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 Alien token with a +1/+1 counter")
    void enteringTheBattlefieldCreatesAlienTokenWithCounter() {
        harness.setHand(player1, List.of(new ReconCraftTheta()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent alien = findPermanents(player1, "Alien").getFirst();
        assertThat(alien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, alien)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, alien)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking after being crewed proliferates")
    void attackingAfterBeingCrewedProliferates() {
        addCreatureReady(player1, new ReconCraftTheta());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(crew.getId()));

        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({ReconCraftTheta.class, CloudfinRaptor.class})
    @DisplayName("The Alien enters as 0/0 before its counter is put on it")
    void alienDoesNotTriggerEvolveBeforeReceivingCounter() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        harness.setHand(player1, List.of(new ReconCraftTheta()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Alien").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack proliferates every counter kind on a chosen opposing permanent and player")
    void attackCanProliferateOpposingPermanentAndPlayer() {
        addCreatureReady(player1, new ReconCraftTheta());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposingCreature.setCounterCount(CounterType.CHARGE, 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(opposingCreature.getId(), player2.getId()));

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack proliferate allows choosing nothing")
    void attackCanProliferateNothing() {
        addCreatureReady(player1, new ReconCraftTheta());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
