package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MagaTraitorToMortals.class)
class MagaTraitorToMortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Maga enters with X +1/+1 counters and makes the target player lose that much life")
    void entersWithCountersAndCausesLifeLoss() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent maga = findPermanent(player1, "Maga, Traitor to Mortals");
        assertThat(maga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Maga's ETB life loss uses the counters on Maga when the ability resolves")
    void etbUsesCountersAtResolution() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent maga = findPermanent(player1, "Maga, Traitor to Mortals");
        maga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Maga can target its controller")
    void canTargetItsController() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, 2, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Maga with X=0 dies and its ETB causes no life loss")
    void zeroXDiesAndCausesNoLifeLoss() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Maga, Traitor to Mortals");
        harness.assertInGraveyard(player1, "Maga, Traitor to Mortals");
    }
}
