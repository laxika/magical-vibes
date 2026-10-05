package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridPals.class})
class PutridPalsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters after your permanent leaves the battlefield")
    void entersWithCountersAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when no permanent left the battlefield")
    void entersWithoutCountersWithoutDisappear() {
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy Disappear")
    void opponentPermanentLeavingDoesNotSatisfyDisappear() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new PutridPals());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Disappear checks departures at entry rather than at casting")
    void departureWhileSpellIsOnStackSatisfiesDisappear() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        harness.setHand(player1, List.of(new PutridPals()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passBothPriorities();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple departures still give exactly two counters")
    void multipleDeparturesDoNotIncreaseCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A departure on a previous turn does not satisfy Disappear")
    void previousTurnDepartureDoesNotSatisfyDisappear() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Disappear applies when Putrid Pals enters without being cast")
    void enteringWithoutCastingGetsCountersImmediately() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new PutridPals());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        Permanent entered = harness.enterBattlefieldAndReturn(player1, new PutridPals());

        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deathtouch kills a blocker whose toughness exceeds the damage dealt")
    void deathtouchKillsLargerBlocker() {
        addCreatureReady(player1, new PutridPals());
        Permanent blocker = addCreatureReady(player2, new PutridPals());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    private void castPutridPals() {
        harness.setHand(player1, List.of(new PutridPals()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findPutridPals() {
        return findPermanent(player1, "Putrid Pals");
    }
}
