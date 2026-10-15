package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @DisplayName("A blocking Lynx survives two combat damage while the attacking Lynx dies")
    void toughnessBonusAppliesDuringCombat() {
        addCreatureReady(player1, new MesaLynx());
        harness.addToBattlefield(player2, new MesaLynx());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Mesa Lynx");
        harness.assertNotOnBattlefield(player1, "Mesa Lynx");
        harness.assertInGraveyard(player1, "Mesa Lynx");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
