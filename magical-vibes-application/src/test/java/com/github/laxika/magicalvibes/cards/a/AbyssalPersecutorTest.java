package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LaboratoryManiac;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class AbyssalPersecutorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller cannot win from an empty-library draw")
    void controllerCannotWin() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.addToBattlefield(player1, new LaboratoryManiac());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Opponents cannot lose while the creature is on the battlefield")
    void opponentsCannotLose() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.setLife(player2, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Controller can still lose the game")
    void controllerCanLose() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
