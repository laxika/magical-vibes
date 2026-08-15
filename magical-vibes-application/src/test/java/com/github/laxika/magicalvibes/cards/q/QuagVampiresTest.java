package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuagVampiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it enters without +1/+1 counters")
    void entersWithoutCountersWithoutMultikicker() {
        harness.setHand(player1, List.of(new QuagVampires()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent quagVampires = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(quagVampires.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter for each multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        Card quagVampires = new QuagVampires();
        harness.setHand(player1, List.of(quagVampires));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}{B}", "{1}{B}"), false);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
