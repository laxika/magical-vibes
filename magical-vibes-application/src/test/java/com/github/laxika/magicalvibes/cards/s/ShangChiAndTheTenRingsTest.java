package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShangChiAndTheTenRings.class, Forest.class})
class ShangChiAndTheTenRingsTest extends BaseCardTest {

    @Test
    @DisplayName("The tenth +1/+1 counter draws five cards and gains 5 life")
    void tenthCounterTriggersDrawAndLifeGain() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        shangChi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // The draw puts the tenth counter on Shang-Chi.
        harness.passBothPriorities(); // The tenth-counter trigger draws five and gains 5 life.
        resolveAllTriggers(); // Resolve the five draw triggers caused by the payoff.

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertLife(player1, 25);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
