package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfGarenbrig.class, GrizzlyBears.class})
class CourtOfGarenbrigTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes the monarch when it enters")
    void becomesMonarchOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGarenbrig());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Distributes counters among up to two creatures and doubles controlled counters as monarch")
    void distributesCountersAndDoublesControlledCountersAsMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGarenbrig());
        resolveAllTriggers();
        Permanent ownFirst = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownSecond = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        ownFirst.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ownSecond.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, ownFirst.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Still doubles controlled counters when no creatures are targeted")
    void doublesControlledCountersWithNoTargets() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGarenbrig());
        resolveAllTriggers();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double counters when its controller is not the monarch")
    void doesNotDoubleCountersWhenNotMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGarenbrig());
        resolveAllTriggers();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
