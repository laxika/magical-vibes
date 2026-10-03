package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DranasEmissary.class})
class DranasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, each opponent loses 1 life and you gain 1 life")
    void drainsEachOpponentDuringControllerUpkeep() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DranasEmissary());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DranasEmissary());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Emissary creates one complete drain trigger")
    void multipleEmissariesTriggerIndependently() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DranasEmissary());
        harness.addToBattlefield(player1, new DranasEmissary());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The upkeep trigger resolves after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DranasEmissary());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Drana's Emissary"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drana's Emissary");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The second player's Emissary drains the first player on its controller's upkeep")
    void drainsRelativeToTriggerController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DranasEmissary());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 21);
    }
}
