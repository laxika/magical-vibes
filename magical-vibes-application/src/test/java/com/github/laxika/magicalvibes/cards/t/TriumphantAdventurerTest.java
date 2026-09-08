package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TriumphantAdventurer.class, GrizzlyBears.class})
class TriumphantAdventurerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Triumphant Adventurer makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        addCreatureReady(player1, new TriumphantAdventurer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("During its controller's turn, first strike lets it survive combat with a Grizzly Bears")
    void firstStrikeDuringControllerTurn() {
        Permanent adventurer = addCreatureReady(player1, new TriumphantAdventurer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(adventurer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("It does not have first strike during an opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent adventurer = addCreatureReady(player1, new TriumphantAdventurer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(adventurer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }
}
