package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumberingTrudge.class})
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

    @Test
    void entersTappedWithTwoStunCountersAtXOne() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Slumbering Trudge");
        assertThat(trudge.isTapped()).isTrue();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void entersUntappedWithoutCountersWhenXExceedsThree() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Slumbering Trudge");
        assertThat(trudge.isTapped()).isFalse();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void enteringWithoutBeingCastUsesZeroForX() {
        Permanent trudge = harness.enterBattlefieldAndReturn(player1, new SlumberingTrudge());

        assertThat(trudge.isTapped()).isTrue();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachUntapStepRemovesOneStunCounterBeforeCreatureCanUntap() {
        harness.setHand(player1, List.of(new SlumberingTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent trudge = findPermanent(player1, "Slumbering Trudge");

        harness.performUntapStep(player2);
        assertThat(trudge.isTapped()).isTrue();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(3);

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player1);
            assertThat(trudge.isTapped()).isTrue();
            assertThat(trudge.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }

        harness.performUntapStep(player1);
        assertThat(trudge.isTapped()).isFalse();
        assertThat(trudge.getCounterCount(CounterType.STUN)).isZero();
    }
}
