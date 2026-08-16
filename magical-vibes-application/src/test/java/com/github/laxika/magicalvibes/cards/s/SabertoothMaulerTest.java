package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SabertoothMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, a creature death adds a counter and untaps Sabertooth Mauler")
    void creatureDeathAddsCounterAndUntaps() {
        Permanent mauler = addReadyMauler();
        mauler.tap();
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing at your end step when no creature died this turn")
    void noCreatureDeathDoesNothing() {
        Permanent mauler = addReadyMauler();
        mauler.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mauler.isTapped()).isTrue();
    }

    private Permanent addReadyMauler() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new SabertoothMauler());
        mauler.setSummoningSick(false);
        return mauler;
    }
}
