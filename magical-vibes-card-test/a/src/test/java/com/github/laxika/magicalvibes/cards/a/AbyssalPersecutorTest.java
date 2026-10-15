package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LaboratoryManiac;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalPersecutor.class, LaboratoryManiac.class})
class AbyssalPersecutorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller cannot win from an empty-library draw")
    void controllerCannotWin() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.addToBattlefield(player1, new LaboratoryManiac());
        harness.setLibrary(player1, List.of());

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

    @Test
    @DisplayName("Opponent can win with Laboratory Maniac despite controlling no Persecutor")
    void opponentCanWinByAlternateCondition() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.addToBattlefield(player2, new LaboratoryManiac());
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent cannot lose to poison until Persecutor leaves")
    void opponentCannotLoseToPoison() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        gd.playerPoisonCounters.put(player2.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A prevented empty-library loss requires a new draw attempt after Persecutor leaves")
    void opponentCannotLoseFromEmptyLibrary() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Controller still loses when drawing from an empty library without a replacement")
    void controllerCanLoseFromEmptyLibrary() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Removing one of two Persecutors does not lift the opponent's loss restriction")
    void remainingCopyStillPreventsLoss() {
        var first = harness.addToBattlefieldAndReturn(player1, new AbyssalPersecutor());
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.setLife(player2, -5);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Persecutors controlled by both players prevent both players from losing")
    void opposingCopiesPreventBothLosses() {
        harness.addToBattlefield(player1, new AbyssalPersecutor());
        harness.addToBattlefield(player2, new AbyssalPersecutor());
        harness.setLife(player1, 0);
        harness.setLife(player2, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
