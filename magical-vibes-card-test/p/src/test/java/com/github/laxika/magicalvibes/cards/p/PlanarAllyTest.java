package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarAlly.class})
class PlanarAllyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Planar Ally makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        addCreatureReady(player1, new PlanarAlly());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Attacking advances the controller's existing dungeon along the chosen path")
    void attackingAdvancesExistingDungeon() {
        addCreatureReady(player1, new PlanarAlly());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Treasure"));
    }

    @Test
    @DisplayName("An opponent's attacking Planar Ally ventures for that opponent")
    void opponentControlsVenture() {
        addCreatureReady(player2, new PlanarAlly());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The attack trigger still ventures after Planar Ally leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent ally = addCreatureReady(player1, new PlanarAlly());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }
}
