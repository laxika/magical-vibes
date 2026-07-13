package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PsychicTransferTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals when the difference is 5 or less")
    void exchangesWhenWithinFive() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PsychicTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Exchanges life totals when the difference is exactly 5")
    void exchangesWhenDifferenceExactlyFive() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PsychicTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Does not exchange life totals when the difference is greater than 5")
    void doesNotExchangeWhenDifferenceTooLarge() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PsychicTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Equal life totals produce no change")
    void equalLifeTotalsNoChange() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PsychicTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
