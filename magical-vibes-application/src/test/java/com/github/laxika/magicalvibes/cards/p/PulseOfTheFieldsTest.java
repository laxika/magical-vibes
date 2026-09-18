package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(PulseOfTheFields.class)
class PulseOfTheFieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life and returns to hand when an opponent still has more life")
    void returnsToHandWhenOpponentStillHasMoreLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        cast();

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, "Pulse of the Fields");
        harness.assertNotInGraveyard(player1, "Pulse of the Fields");
    }

    @Test
    @DisplayName("Gains 4 life and goes to the graveyard when no opponent has more life afterward")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreLifeAfterward() {
        harness.setLife(player1, 18);
        harness.setLife(player2, 20);

        cast();

        harness.assertLife(player1, 22);
        harness.assertNotInHand(player1, "Pulse of the Fields");
        harness.assertInGraveyard(player1, "Pulse of the Fields");
    }

    @Test
    @DisplayName("Goes to the graveyard when life totals are equal after the life gain")
    void goesToGraveyardWhenLifeTotalsAreEqualAfterward() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);

        cast();

        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Pulse of the Fields");
        harness.assertInGraveyard(player1, "Pulse of the Fields");
    }

    private void cast() {
        harness.castFromHand(player1, new PulseOfTheFields(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
