package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.SundialOfTheInfinite;
import com.github.laxika.magicalvibes.cards.u.UginsNexus;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastChance.class, PlatinumAngel.class})
class LastChanceTest extends BaseCardTest {

    private void castLastChance() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LastChance(), "{R}{R}");
        harness.passBothPriorities();
    }

    private void advanceToExtraTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToOpponentTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Resolving queues an extra turn and registers a delayed 'lose the game'")
    void resolvingQueuesExtraTurnAndDelayedLoss() {
        int turnBefore = gd.turnNumber;
        castLastChance();

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
        castLastChance();

        // Reach this turn's end step without advancing the turn number.
        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
    }

    @Test
    @DisplayName("You lose the game at the beginning of the extra turn's end step")
    void extraTurnEndStepTriggersLoss() {
        castLastChance();

        // End the current turn -> begin the extra turn (still player1, next turn number).
        advanceToExtraTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Reach the extra turn's end step -> delayed loss fires onto the stack.
        advanceToEndStep();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities(); // resolve the loss

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the game"));
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Platinum Angel keeps you from losing at the extra turn's end step")
    void platinumAngelPreventsLoss() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        castLastChance();

        advanceToExtraTurn();

        advanceToEndStep();
        harness.passBothPriorities(); // resolve the loss trigger

        // Can't-lose: the trigger resolves but the player stays in the game.
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed(UginsNexus.class)
    @DisplayName("Skipping the gained extra turn prevents the delayed loss")
    void skippingGainedExtraTurnPreventsLoss() {
        harness.addToBattlefield(player1, new UginsNexus());
        castLastChance();

        advanceToOpponentTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Each delayed loss is tied to the extra turn created by its own spell")
    void delayedLossesTrackTheirOwnExtraTurns() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setHand(player1, List.of(new LastChance(), new LastChance()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(2);

        advanceToExtraTurn();
        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed(SundialOfTheInfinite.class)
    @DisplayName("Ending the extra turn before its end step prevents the delayed loss")
    void endingExtraTurnBeforeEndStepPreventsLoss() {
        Permanent sundial = harness.addToBattlefieldAndReturn(player1, new SundialOfTheInfinite());
        sundial.setSummoningSick(false);
        castLastChance();

        advanceToExtraTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
