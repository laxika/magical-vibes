package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MesaLynx.class)
class MesaLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+2 during an opponent's turn")
    void boostedOnOpponentTurn() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new MesaLynx());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(3);
    }

    @Test
    @DisplayName("Is not boosted during its controller's turn")
    void notBoostedOnControllerTurn() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new MesaLynx());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(1);
    }

    @Test
    @DisplayName("The bonus follows the creature's controller")
    void bonusFollowsController() {
        Permanent ownLynx = harness.addToBattlefieldAndReturn(player1, new MesaLynx());
        Permanent enemyLynx = harness.addToBattlefieldAndReturn(player2, new MesaLynx());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectiveToughness(gd, ownLynx)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyLynx)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectiveToughness(gd, ownLynx)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enemyLynx)).isEqualTo(1);
    }
}
