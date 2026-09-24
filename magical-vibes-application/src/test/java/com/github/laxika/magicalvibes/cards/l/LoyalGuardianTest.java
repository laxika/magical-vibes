package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalGuardian.class, GrizzlyBears.class})
class LoyalGuardianTest extends BaseCardTest {

    @Test
    void putsCountersOnEachCreatureWhileControllingCommander() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCountersWithoutControllingCommander() {
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToBeginningOfCombat(player2);

        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
