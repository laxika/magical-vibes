package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadameNullPowerBroker.class, GrizzlyBears.class})
class MadameNullPowerBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying life equal to the entering creature's power puts that many counters on it")
    void paysLifeAndPutsCountersOnEnteringCreature() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new MadameNullPowerBroker());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }
}
