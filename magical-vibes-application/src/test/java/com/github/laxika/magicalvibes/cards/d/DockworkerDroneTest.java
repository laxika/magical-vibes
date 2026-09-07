package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DockworkerDrone.class, GrizzlyBears.class})
class DockworkerDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent drone = castDrone();

        assertThat(drone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, puts its +1/+1 counters on a creature you control")
    void deathTransfersPlusOneCountersToControlledCreature() {
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());
        Permanent drone = addCreatureReady(player1, new DockworkerDrone());
        drone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        drone.setCounterCount(CounterType.CHARGE, 3);

        removeDrone(drone);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The death trigger still targets a creature when it has no +1/+1 counters")
    void deathTriggerWithNoCounters() {
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());
        Permanent drone = addCreatureReady(player1, new DockworkerDrone());
        drone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        removeDrone(drone);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void removeDrone(Permanent drone) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, drone));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent castDrone() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DockworkerDrone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Dockworker Drone");
    }
}
