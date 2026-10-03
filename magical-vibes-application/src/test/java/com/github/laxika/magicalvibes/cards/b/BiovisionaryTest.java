package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SapphireDrake;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biovisionary.class, SapphireDrake.class})
class BiovisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at the end step with four Biovisionaries")
    void winsWithFourCopies() {
        addCopies(player1, 4);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger with only three Biovisionaries")
    void noTriggerWithThreeCopies() {
        addCopies(player1, 3);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Other creatures do not count toward the four")
    void otherCreaturesDoNotCount() {
        addCopies(player1, 3);
        harness.addToBattlefield(player1, new SapphireDrake());

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Copies controlled by an opponent do not count")
    void opponentCopiesDoNotCount() {
        addCopies(player1, 2);
        addCopies(player2, 2);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Triggers at an opponent's end step too")
    void triggersOnOpponentEndStep() {
        addCopies(player1, 4);

        advanceToEndStepAndResolve(player2);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each copy triggers when the controller has more than four copies")
    void winsWithFiveCopies() {
        addCopies(player1, 5);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(5);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not win if fewer than four copies remain when the abilities resolve")
    void rechecksCountAtResolution() {
        addCopies(player1, 4);
        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(4);

        gd.playerBattlefields.get(player1.getId()).removeLast();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Adding a fourth copy after the end step begins does not trigger the ability")
    void fourthCopyArrivesTooLate() {
        addCopies(player1, 3);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        addCopies(player1, 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The nonactive player wins first when both players have four copies")
    void nonactivePlayerWinsWithSimultaneousTriggers() {
        addCopies(player1, 4);
        addCopies(player2, 4);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(8);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void addCopies(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Biovisionary());
        }
    }
}
