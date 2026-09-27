package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.InspiringStatuary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MotivatedMuralist.class, InspiringStatuary.class})
class MotivatedMuralistTest extends BaseCardTest {

    @Test
    void conjuresInspiringStatuaryIntoHand() {
        harness.setHand(player1, List.of(new MotivatedMuralist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, "Inspiring Statuary");
    }
}
