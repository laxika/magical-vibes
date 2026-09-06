package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SavantiRomeroTimesExile.class)
class SavantiRomeroTimesExileTest extends BaseCardTest {

    @Test
    void putsACounterThenDrawsAndLosesLifeForAllCounters() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        savanti.setCounterCount(CounterType.CHARGE, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());

        runBeginningOfCombat(player1);

        assertThat(savanti.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(savanti.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life - 2);
    }

    @Test
    void doesNotTriggerOnOpponentsCombat() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());

        runBeginningOfCombat(player2);

        assertThat(savanti.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life);
    }

    private void runBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
