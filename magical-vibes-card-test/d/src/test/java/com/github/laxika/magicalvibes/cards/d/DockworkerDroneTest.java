package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DockworkerDrone.class, IntrepidTenderfoot.class})
class DockworkerDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent drone = castDrone();

        assertThat(drone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, puts all its counters on a creature you control")
    void deathTransfersAllCounterTypesToControlledCreature() {
        Permanent recipient = addCreatureReady(player1, new IntrepidTenderfoot());
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
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The death trigger still targets a creature when it has no +1/+1 counters")
    void deathTriggerWithNoCounters() {
        Permanent recipient = addCreatureReady(player1, new IntrepidTenderfoot());
        Permanent drone = addCreatureReady(player1, new DockworkerDrone());
        drone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        removeDrone(drone);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with its counter even when not cast")
    void entersWithoutBeingCastWithCounter() {
        Permanent drone = harness.enterBattlefieldAndReturn(player1, new DockworkerDrone());

        assertThat(drone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Transfers charge counters even with no +1/+1 counters")
    void deathTransfersOnlyChargeCounters() {
        Permanent recipient = addCreatureReady(player1, new IntrepidTenderfoot());
        recipient.setCounterCount(CounterType.CHARGE, 2);
        Permanent drone = addCreatureReady(player1, new DockworkerDrone());
        drone.setCounterCount(CounterType.CHARGE, 3);

        removeDrone(drone);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death ability cannot target an opponent's creature")
    void deathTargetsOnlyControlledCreatures() {
        Permanent recipient = addCreatureReady(player1, new IntrepidTenderfoot());
        Permanent opponentCreature = addCreatureReady(player2, new IntrepidTenderfoot());
        Permanent drone = addCreatureReady(player1, new DockworkerDrone());
        drone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        removeDrone(drone);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
        harness.castFromHand(player1, new DockworkerDrone(), "{1}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Dockworker Drone");
    }
}
