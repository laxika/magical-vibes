package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingFistOfficer.class, GrizzlyBears.class})
class FlamingFistOfficerTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureYouControlLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(ally);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsOneCounterForEachAllyThatLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent firstAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAlly = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(firstAlly);
        removeFromBattlefield(secondAlly);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotGetCounterWhenOpponentsCreatureLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        removeFromBattlefield(opponentCreature);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsCounterWhenAnotherOfficerIsReturnedToHand() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent ally = addCreatureReady(player1, new FlamingFistOfficer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ally));
        resolveAllTriggers();

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Flaming Fist Officer");
    }

    @Test
    void getsCounterWhenAnotherOfficerIsExiled() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent ally = addCreatureReady(player1, new FlamingFistOfficer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ally));
        resolveAllTriggers();

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(officer);
    }

    @Test
    void doesNotTriggerForItsOwnDeparture() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, officer));

        assertThat(gd.stack).isEmpty();
        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bothOfficersTriggerWhenTheyLeaveSimultaneously() {
        Permanent firstOfficer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent secondOfficer = addCreatureReady(player1, new FlamingFistOfficer());

        harness.inMutationScope(() -> {
            var removal = harness.getPermanentRemovalService();
            removal.performSimultaneousRemovals(gd, List.of(firstOfficer, secondOfficer), () -> {
                removal.removePermanentToGraveyard(gd, firstOfficer);
                removal.removePermanentToGraveyard(gd, secondOfficer);
            });
        });

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(firstOfficer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondOfficer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }
}
