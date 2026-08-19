package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
