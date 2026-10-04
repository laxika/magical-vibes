package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameRift.class, Mossdog.class})
class FlameRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Flame Rift deals 4 damage to each player")
    void dealsFourDamageToEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new FlameRift(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Flame Rift does not damage creatures")
    void doesNotDamageCreatures() {
        harness.addToBattlefield(player1, new Mossdog());
        harness.addToBattlefield(player2, new Mossdog());

        harness.castFromHand(player1, new FlameRift(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mossdog");
        harness.assertOnBattlefield(player2, "Mossdog");
    }

    @Test
    @DisplayName("Flame Rift draws the game when both players take lethal damage")
    void simultaneousLethalDamageDrawsGame() {
        harness.setLife(player1, 4);
        harness.setLife(player2, 3);

        harness.castFromHand(player1, new FlameRift(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
