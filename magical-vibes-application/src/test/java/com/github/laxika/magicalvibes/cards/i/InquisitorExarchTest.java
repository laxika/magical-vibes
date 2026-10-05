package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorExarch.class})
class InquisitorExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Non-cast entry lets the controller choose life loss and its opponent target")
    void nonCastEntryCanChooseLifeLoss() {
        harness.enterBattlefieldAndReturn(player1, new InquisitorExarch());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Target opponent loses 2 life");
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Non-cast entry gains life for the entering creature's controller")
    void nonCastEntryGainsLifeForOtherController() {
        harness.enterBattlefieldAndReturn(player2, new InquisitorExarch());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "You gain 2 life");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Nested
    @CardUsed({InquisitorExarch.class})
    @DisplayName("Mode 1: You gain 2 life")
    class GainLifeMode {

        @Test
        @DisplayName("Controller gains 2 life")
        void controllerGains2Life() {
            castWithGainLifeMode();
            resolveCreatureAndChooseGainLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertLife(player1, 22);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Life gain works with non-default life totals")
        void lifeGainWithCustomTotals() {
            harness.setLife(player1, 5);

            castWithGainLifeMode();
            resolveCreatureAndChooseGainLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertLife(player1, 7);
        }

        @Test
        @DisplayName("Inquisitor Exarch enters the battlefield when choosing gain life mode")
        void exarchEntersBattlefield() {
            castWithGainLifeMode();
            resolveCreatureAndChooseGainLifeMode();

            harness.assertOnBattlefield(player1, "Inquisitor Exarch");
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackIsEmptyAfterResolution() {
            castWithGainLifeMode();
            resolveCreatureAndChooseGainLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.stack).isEmpty();
        }

        private void castWithGainLifeMode() {
            harness.castFromHand(player1, new InquisitorExarch(), "{W}{W}");
        }

        private void resolveCreatureAndChooseGainLifeMode() {
            harness.passBothPriorities();
            harness.handleListChoice(player1, "You gain 2 life");
        }
    }

    @Nested
    @CardUsed({InquisitorExarch.class})
    @DisplayName("Mode 2: Target opponent loses 2 life")
    class LoseLifeMode {

        @Test
        @DisplayName("Target opponent loses 2 life")
        void targetOpponentLoses2Life() {
            castWithLoseLifeMode();
            resolveCreatureAndChooseLoseLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertLife(player2, 18);
            harness.assertLife(player1, 20);
        }

        @Test
        @DisplayName("Life loss works with non-default life totals")
        void lifeLossWithCustomTotals() {
            harness.setLife(player2, 5);

            castWithLoseLifeMode();
            resolveCreatureAndChooseLoseLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertLife(player2, 3);
        }

        @Test
        @DisplayName("Inquisitor Exarch enters the battlefield when choosing lose life mode")
        void exarchEntersBattlefield() {
            castWithLoseLifeMode();
            resolveCreatureAndChooseLoseLifeMode();

            harness.assertOnBattlefield(player1, "Inquisitor Exarch");
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackIsEmptyAfterResolution() {
            castWithLoseLifeMode();
            resolveCreatureAndChooseLoseLifeMode();
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.stack).isEmpty();
        }

        private void castWithLoseLifeMode() {
            harness.castFromHand(player1, new InquisitorExarch(), "{W}{W}");
        }

        private void resolveCreatureAndChooseLoseLifeMode() {
            harness.passBothPriorities();
            harness.handleListChoice(player1, "Target opponent loses 2 life");
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }
}
