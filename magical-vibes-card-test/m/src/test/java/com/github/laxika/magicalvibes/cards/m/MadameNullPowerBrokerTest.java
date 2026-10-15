package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadameNullPowerBroker.class, GrizzlyBears.class})
class MadameNullPowerBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying life equal to the entering creature's power puts that many counters on it")
    void paysLifeAndPutsCountersOnEnteringCreature() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(broker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The power is checked when the ability resolves")
    void usesPowerAtResolution() {
        harness.addToBattlefieldAndReturn(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the payment leaves life and counters unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("The trigger only watches creatures controlled by Madame Null's controller")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }
    @Test
    @DisplayName("Madame Null does not trigger for its own entry")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new MadameNullPowerBroker(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanent(player1, "Madame Null, Power Broker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Insufficient life prevents payment and counter placement")
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A departed creature uses its power immediately before leaving for the payment")
    void usesLastKnownPowerWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Madame Null, Power Broker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing Madame Null does not stop its already triggered ability")
    void triggerSurvivesSourceRemoval() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, broker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
