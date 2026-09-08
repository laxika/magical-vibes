package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrystallineGiant.class)
class CrystallineGiantTest extends BaseCardTest {

    private static final List<CounterType> COUNTER_TYPES = List.of(
            CounterType.FLYING,
            CounterType.FIRST_STRIKE,
            CounterType.DEATHTOUCH,
            CounterType.HEXPROOF,
            CounterType.LIFELINK,
            CounterType.MENACE,
            CounterType.REACH,
            CounterType.TRAMPLE,
            CounterType.VIGILANCE,
            CounterType.PLUS_ONE_PLUS_ONE
    );

    @Test
    void putsTheOnlyMissingCounterOnIt() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.stream()
                .filter(counterType -> counterType != CounterType.VIGILANCE)
                .forEach(counterType -> giant.setCounterCount(counterType, 1));

        resolveBeginningOfCombat(player1);

        assertThat(giant.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(10);
    }

    @Test
    void doesNotTriggerOnOpponentTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());

        resolveBeginningOfCombat(player2);

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isZero();
    }

    @Test
    void doesNothingWhenItHasEveryListedCounter() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.forEach(counterType -> giant.setCounterCount(counterType, 1));

        resolveBeginningOfCombat(player1);

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(10);
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
