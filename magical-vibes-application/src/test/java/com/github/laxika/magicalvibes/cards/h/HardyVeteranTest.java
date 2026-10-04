package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardyVeteran.class})
class HardyVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+2 during its controller's turn")
    void boostedOnControllerTurn() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(4);
    }

    @Test
    @DisplayName("Is not boosted during another player's turn")
    void notBoostedOnOpponentTurn() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus follows the creature's controller")
    void bonusFollowsController() {
        Permanent ownVeteran = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());
        Permanent enemyVeteran = harness.addToBattlefieldAndReturn(player2, new HardyVeteran());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectiveToughness(gd, ownVeteran)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enemyVeteran)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectiveToughness(gd, ownVeteran)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyVeteran)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus applies throughout the controller's turn, including cleanup")
    void bonusAppliesDuringEveryStepOfControllerTurn() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());

        for (TurnStep step : TurnStep.values()) {
            harness.forceStep(step);
            harness.forceActivePlayer(player1);
            assertThat(gqs.getEffectivePower(gd, veteran)).as("power during %s", step).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, veteran)).as("toughness during %s", step).isEqualTo(4);

            harness.forceActivePlayer(player2);
            assertThat(gqs.getEffectiveToughness(gd, veteran)).as("opponent's %s", step).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Multiple veterans each boost only themselves")
    void multipleVeteransDoNotBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }
}
