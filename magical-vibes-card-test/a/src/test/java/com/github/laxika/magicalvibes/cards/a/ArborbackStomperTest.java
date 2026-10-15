package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborbackStomper.class})
class ArborbackStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 5 life")
    void entryGainsFiveLife() {
        harness.castFromHand(player1, new ArborbackStomper(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.castFromHand(player1, new ArborbackStomper(), "{3}{G}{G}");

        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arborback Stomper");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player's Stomper gains life only for its controller")
    void secondPlayerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ArborbackStomper(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 25);
    }
}
