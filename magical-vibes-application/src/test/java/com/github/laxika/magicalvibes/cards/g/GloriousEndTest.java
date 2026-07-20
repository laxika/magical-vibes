package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GloriousEndTest extends BaseCardTest {

    private void castGloriousEnd() {
        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving ends the turn and registers a delayed 'lose the game'")
    void resolvingEndsTurnAndRegistersDelayedLoss() {
        int turnBefore = gd.turnNumber;
        castGloriousEnd();

        // "End the turn" empties the stack (including Glorious End itself).
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("The turn ends.")).isTrue();

        List<LoseGameAtEndStep> pending = gd.getDelayedActions(LoseGameAtEndStep.class);
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(pending.getFirst().registeredTurnNumber()).isEqualTo(turnBefore);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The intervening opponent's end step does not trigger the loss")
    void opponentEndStepDoesNotTriggerLoss() {
        int turnBefore = gd.turnNumber;
        castGloriousEnd();

        // Reach the opponent's end step on the very next turn.
        gd.turnNumber = turnBefore + 1;
        gd.activePlayerId = player2.getId();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> player2's END_STEP

        // "your next end step" skips opponents' end steps: the loss stays scheduled.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
    }

    @Test
    @DisplayName("You lose the game at the beginning of your own next end step")
    void ownNextEndStepTriggersLoss() {
        int turnBefore = gd.turnNumber;
        castGloriousEnd();

        // Skip past the opponent's turn to your own next turn's end step.
        gd.turnNumber = turnBefore + 2;
        gd.activePlayerId = player1.getId();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> player1's END_STEP
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities(); // resolve the loss

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the game"));
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Platinum Angel keeps you from losing at your next end step")
    void platinumAngelPreventsLoss() {
        int turnBefore = gd.turnNumber;
        harness.addToBattlefield(player1, new PlatinumAngel());
        castGloriousEnd();

        gd.turnNumber = turnBefore + 2;
        gd.activePlayerId = player1.getId();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> player1's END_STEP
        harness.passBothPriorities(); // resolve the loss trigger

        // Can't-lose: the trigger resolves but the player stays in the game.
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
