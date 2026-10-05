package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NastyLittleRabbit.class, AirElemental.class, GrizzlyBears.class})
class NastyLittleRabbitTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when you control a creature with power 4 or greater")
    void putsCounterWithFerocious() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        addCreatureReady(player1, new AirElemental());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can count itself when its power is 4 or greater")
    void countsItselfAtPowerFour() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        rabbit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerBelowPowerThreshold() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature does not satisfy ferocious")
    void opponentCreatureDoesNotCount() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        addCreatureReady(player2, new AirElemental());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        addCreatureReady(player1, new AirElemental());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rechecks the power condition when the combat trigger resolves")
    void doesNothingIfPowerFallsBelowFourBeforeResolution() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        rabbit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        rabbit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gaining sufficient power after combat begins does not create a trigger")
    void doesNotTriggerRetroactively() {
        Permanent rabbit = addCreatureReady(player1, new NastyLittleRabbit());
        rabbit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        rabbit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Rabbit puts a counter only on itself")
    void multipleRabbitsReceiveTheirOwnCounters() {
        Permanent first = addCreatureReady(player1, new NastyLittleRabbit());
        Permanent second = addCreatureReady(player1, new NastyLittleRabbit());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
