package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaunchDefenders.class})
class StaunchDefendersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield puts the life-gain trigger on the stack")
    void entryTriggersLifeGain() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Staunch Defenders");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolving the ETB trigger gains 4 life")
    void entryGainsFourLife() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("The entering creature's controller gains the life")
    void controllerGainsLife() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 13);

        harness.castFromHand(player2, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 17);
    }
}
