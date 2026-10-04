package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireTempest.class, GrizzlyBears.class, RedwoodTreefolk.class, WallOfGranite.class})
class FireTempestTest extends BaseCardTest {

    private void castFireTempest() {
        harness.castFromHand(player1, new FireTempest(), "{5}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Fire Tempest deals 6 damage to each player")
    void dealsSixToEachPlayer() {
        castFireTempest();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Fire Tempest kills creatures with 6 or less toughness")
    void killsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new WallOfGranite());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new WallOfGranite());

        castFireTempest();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Redwood Treefolk");
        harness.assertOnBattlefield(player1, "Wall of Granite");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Redwood Treefolk");
        harness.assertOnBattlefield(player2, "Wall of Granite");
    }

    @Test
    @DisplayName("Fire Tempest can kill the caster")
    void canKillCaster() {
        harness.setLife(player1, 6);

        castFireTempest();

        GameData gd = harness.getGameData();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Surviving creatures retain damage for another Fire Tempest in the same turn")
    void survivingCreaturesRetainDamage() {
        harness.addToBattlefield(player1, new WallOfGranite());
        harness.addToBattlefield(player2, new WallOfGranite());

        castFireTempest();

        harness.assertOnBattlefield(player1, "Wall of Granite");
        harness.assertOnBattlefield(player2, "Wall of Granite");

        castFireTempest();

        harness.assertNotOnBattlefield(player1, "Wall of Granite");
        harness.assertNotOnBattlefield(player2, "Wall of Granite");
        harness.assertInGraveyard(player1, "Wall of Granite");
        harness.assertInGraveyard(player2, "Wall of Granite");
        harness.assertLife(player1, 8);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Fire Tempest draws the game when both players take lethal damage")
    void drawsWhenBothPlayersTakeLethalDamage() {
        harness.setLife(player1, 6);
        harness.setLife(player2, 6);

        castFireTempest();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
