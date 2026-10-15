package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsOath.class, PlatinumAngel.class, CaptureOfJingzhou.class})
class WarriorsOathTest extends BaseCardTest {

    private void castWarriorsOath() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WarriorsOath(), "{R}{R}");
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
    }

    @Test
    @DisplayName("Resolving queues an extra turn and registers a delayed 'lose the game'")
    void resolvingQueuesExtraTurnAndDelayedLoss() {
        int turnBefore = gd.turnNumber;
        castWarriorsOath();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        List<LoseGameAtEndStep> pending = gd.getDelayedActions(LoseGameAtEndStep.class);
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(pending.getFirst().registeredTurnNumber()).isEqualTo(turnBefore);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The current turn's own end step does not trigger the loss")
    void currentTurnEndStepDoesNotTriggerLoss() {
        castWarriorsOath();

        // Reach this turn's end step without advancing the turn number.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
    }

    @Test
    @DisplayName("You lose the game at the beginning of the extra turn's end step")
    void extraTurnEndStepTriggersLoss() {
        castWarriorsOath();

        // End the current turn -> begin the extra turn (still player1, next turn number).
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Reach the extra turn's end step -> delayed loss fires onto the stack.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP
        assertThat(gd.stack).isNotEmpty();

        harness.withAutoStop(gd.currentStep, harness::passBothPriorities); // resolve the loss

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the game"));
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Platinum Angel keeps you from losing at the extra turn's end step")
    void platinumAngelPreventsLoss() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        castWarriorsOath();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities); // resolve the loss trigger

        // Can't-lose: the trigger resolves but the player stays in the game.
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each delayed loss waits for the extra turn created by its own spell")
    void delayedLossesTrackTheirOwnExtraTurns() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        castWarriorsOath();
        castWarriorsOath();

        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(2);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> first extra turn's END_STEP

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> second extra turn's END_STEP

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A later extra-turn spell does not make the Oath loss trigger early")
    void insertedExtraTurnDoesNotTriggerLoss() {
        castWarriorsOath();
        harness.castFromHand(player1, new CaptureOfJingzhou(), "{3}{U}{U}");
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The second player receives the extra turn and loses to their own Oath")
    void secondPlayerReceivesExtraTurnAndLoses() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WarriorsOath(), "{R}{R}");
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
