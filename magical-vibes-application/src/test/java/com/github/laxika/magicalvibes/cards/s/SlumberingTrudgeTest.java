package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlumberingTrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=0 enters tapped with three stun counters")
    void entersTappedWithThreeStunCountersAtZeroX() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Slumbering Trudge");
        assertThat(trudge.isTapped()).isTrue();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=2 enters tapped with one stun counter")
    void entersTappedWithOneStunCounterAtXTwo() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Slumbering Trudge");
        assertThat(trudge.isTapped()).isTrue();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X=3 enters untapped without stun counters")
    void entersUntappedWithoutStunCountersAtXThree() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Slumbering Trudge");
        assertThat(trudge.isTapped()).isFalse();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isZero();
    }
}
