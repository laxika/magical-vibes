package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class LightkeeperOfEmeriaTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it gains no life")
    void gainsNoLifeWithoutMultikicker() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("It gains 2 life for one multikicker payment")
    void gainsTwoLifeForOneMultikickerPayment() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castWithMultikickerPayments(List.of("{W}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("It gains 2 life for each multikicker payment")
    void gainsTwoLifePerMultikickerPayment() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castWithMultikickerPayments(List.of("{W}", "{W}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    private void castWithMultikickerPayments(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
