package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrogButler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperShredder.class, FrogButler.class, Forest.class, Humility.class})
class SuperShredderTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureLeavesTheBattlefield() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent frog = harness.addToBattlefieldAndReturn(player2, new FrogButler());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, frog));
        harness.passBothPriorities();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenAnotherNoncreaturePermanentLeavesTheBattlefield() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        harness.passBothPriorities();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenAnotherPermanentIsExiled() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, forest));
        resolveAllTriggers();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenAnotherPermanentReturnsToHand() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        resolveAllTriggers();

        harness.assertInHand(player2, "Forest");
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsOneCounterForEachPermanentLeavingSimultaneously() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        var removal = harness.getPermanentRemovalService();

        harness.inMutationScope(() -> removal.performSimultaneousRemovals(
                gd, List.of(firstForest, secondForest), () -> {
                    removal.removePermanentToGraveyard(gd, firstForest);
                    removal.removePermanentToGraveyard(gd, secondForest);
                }));
        resolveAllTriggers();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForItsOwnDeparture() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, shredder));

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Super Shredder");
    }

    @Test
    void triggersForOtherPermanentsLeavingSimultaneouslyWithIt() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        var removal = harness.getPermanentRemovalService();

        harness.inMutationScope(() -> removal.performSimultaneousRemovals(
                gd, List.of(shredder, forest), () -> {
                    removal.removePermanentToGraveyard(gd, shredder);
                    removal.removePermanentToGraveyard(gd, forest);
                }));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Super Shredder");
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({Humility.class})
    void doesNotTriggerWhileHumilityRemovesItsAbility() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        harness.addToBattlefield(player2, new Humility());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        resolveAllTriggers();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({Humility.class})
    void doesNotTriggerWhenHumilityLeavesButTriggersForLaterDepartures() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent humility = harness.addToBattlefieldAndReturn(player2, new Humility());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, humility));
        resolveAllTriggers();
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        resolveAllTriggers();
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new SuperShredder());
        addCreatureReady(player2, new FrogButler());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new SuperShredder());
        Permanent first = addCreatureReady(player2, new FrogButler());
        Permanent second = addCreatureReady(player2, new FrogButler());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void pendingTriggerDoesNotPutCounterOnSourceAfterItLeavesAndReturns() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SuperShredder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, shredder));
        harness.setHand(player1, List.of());
        Permanent returnedShredder = harness.addToBattlefieldAndReturn(player1, shredder.getCard());
        resolveAllTriggers();

        assertThat(returnedShredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
