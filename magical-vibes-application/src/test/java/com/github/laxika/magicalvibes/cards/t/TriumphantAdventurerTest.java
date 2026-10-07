package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphantAdventurer.class, HillGiantHerdgorger.class})
class TriumphantAdventurerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Triumphant Adventurer makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        addCreatureReady(player1, new TriumphantAdventurer());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("During its controller's turn, first strike lets it survive combat with a Hill Giant Herdgorger")
    void firstStrikeDuringControllerTurn() {
        Permanent adventurer = addCreatureReady(player1, new TriumphantAdventurer());
        Permanent giant = addCreatureReady(player2, new HillGiantHerdgorger());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(adventurer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
    }

    @Test
    @DisplayName("It does not have first strike during an opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent adventurer = addCreatureReady(player1, new TriumphantAdventurer());
        Permanent giant = addCreatureReady(player2, new HillGiantHerdgorger());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(adventurer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
    }

    @Test
    @DisplayName("Attacking advances an existing dungeon along the chosen path")
    void attackingAdvancesExistingDungeon() {
        addCreatureReady(player1, new TriumphantAdventurer());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's attacking Adventurer ventures for that opponent")
    void opponentControlsVenture() {
        addCreatureReady(player2, new TriumphantAdventurer());

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
    @DisplayName("The attack trigger still ventures after its source leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent adventurer = addCreatureReady(player1, new TriumphantAdventurer());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(adventurer);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }
}
