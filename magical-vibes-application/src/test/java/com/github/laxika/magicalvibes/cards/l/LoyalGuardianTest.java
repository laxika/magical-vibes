package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalGuardian.class, GrizzlyBears.class})
class LoyalGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant puts a +1/+1 counter on each creature you control at combat")
    void putsCountersOnYourCreaturesWhenYouControlYourCommander() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant does nothing without a commander")
    void doesNotTriggerWithoutYourCommander() {
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant does nothing during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, commander);

        advanceToCombat(player2);

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
