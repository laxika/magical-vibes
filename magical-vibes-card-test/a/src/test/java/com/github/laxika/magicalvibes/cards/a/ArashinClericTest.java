package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArashinCleric.class})
class ArashinClericTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 3 life")
    void entersGainsThreeLife() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new ArashinCleric(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Life gain waits for the enters trigger to resolve")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castFromHand(player1, new ArashinCleric(), "{1}{W}");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arashin Cleric");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void enteringWithoutCastingGainsLifeForOpponent() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.enterBattlefieldAndReturn(player2, new ArashinCleric());

        harness.assertLife(player2, 15);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Cleric entering creates its own life gain trigger")
    void multipleClericsGainLifeIndependently() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.enterBattlefieldAndReturn(player1, new ArashinCleric());
        harness.enterBattlefieldAndReturn(player1, new ArashinCleric());

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }
}
