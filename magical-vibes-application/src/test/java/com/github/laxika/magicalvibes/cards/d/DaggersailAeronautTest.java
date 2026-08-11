package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaggersailAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void hasFlyingDuringControllerTurnOnly() {
        Permanent aeronaut = harness.addToBattlefieldAndReturn(player1, new DaggersailAeronaut());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying follows the controller, not the card owner")
    void flyingFollowsController() {
        Permanent ownAeronaut = harness.addToBattlefieldAndReturn(player1, new DaggersailAeronaut());
        Permanent enemyAeronaut = harness.addToBattlefieldAndReturn(player2, new DaggersailAeronaut());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, ownAeronaut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyAeronaut, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, ownAeronaut, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemyAeronaut, Keyword.FLYING)).isTrue();
    }
}
