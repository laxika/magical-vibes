package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BasriKet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdherentOfHope.class, BasriKet.class})
class AdherentOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller has a Basri planeswalker")
    void putsCounterWithBasriPlaneswalker() {
        Permanent adherent = addCreatureReady(player1, new AdherentOfHope());
        harness.addToBattlefieldAndReturn(player1, new BasriKet())
                .setCounterCount(CounterType.LOYALTY, 3);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(adherent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without a Basri planeswalker")
    void doesNotTriggerWithoutBasriPlaneswalker() {
        Permanent adherent = addCreatureReady(player1, new AdherentOfHope());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(adherent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Permanent adherent = addCreatureReady(player1, new AdherentOfHope());
        harness.addToBattlefieldAndReturn(player1, new BasriKet())
                .setCounterCount(CounterType.LOYALTY, 3);

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(adherent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
