package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnarlidPackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without +1/+1 counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gnarlidPack = findGnarlidPack();
        assertThat(gnarlidPack).isNotNull();
        assertThat(gnarlidPack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}{G}", "{1}{G}"), false);
        harness.passBothPriorities();

        Permanent gnarlidPack = findGnarlidPack();
        assertThat(gnarlidPack).isNotNull();
        assertThat(gnarlidPack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent findGnarlidPack() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gnarlid Pack"))
                .findFirst()
                .orElse(null);
    }
}
