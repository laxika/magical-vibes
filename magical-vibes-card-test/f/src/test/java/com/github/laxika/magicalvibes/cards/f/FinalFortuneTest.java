package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.u.UginsNexus;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalFortune.class, Counterspell.class, PlatinumAngel.class, UginsNexus.class})
class FinalFortuneTest extends BaseCardTest {

    private void castFinalFortune() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FinalFortune(), "{R}{R}");
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Resolving queues an extra turn and registers a delayed 'lose the game'")
    void resolvingQueuesExtraTurnAndDelayedLoss() {
        int turnBefore = gd.turnNumber;
        castFinalFortune();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        List<LoseGameAtEndStep> pending = gd.getDelayedActions(LoseGameAtEndStep.class);
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(pending.getFirst().registeredTurnNumber()).isEqualTo(turnBefore);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Casting during an opponent's turn gives you the extra turn after it")
    void castingDuringOpponentsTurnGivesControllerTheExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.castFromHand(player1, new FinalFortune(), "{R}{R}");
            harness.passBothPriorities();

            assertThat(gd.extraTurns).containsExactly(player1.getId());

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());

            advanceToEndStep();
            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities();

            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("Countering it prevents both the extra turn and delayed loss")
    void counteredFinalFortuneDoesNotCreateExtraTurnOrDelayedLoss() {
        FinalFortune finalFortune = new FinalFortune();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(finalFortune));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, finalFortune.getId());
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(finalFortune.getId()));
    }

    @Test
    @DisplayName("The current turn's own end step does not trigger the loss")
    void currentTurnEndStepDoesNotTriggerLoss() {
        castFinalFortune();

        // Reach this turn's end step without advancing the turn number.
        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
    }

    @Test
    @DisplayName("You lose the game at the beginning of the extra turn's end step")
    void extraTurnEndStepTriggersLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            castFinalFortune();

            // End the current turn -> begin the extra turn (still player1, next turn number).
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());

            // Reach the extra turn's end step -> delayed loss fires onto the stack.
            advanceToEndStep();
            assertThat(gd.stack).isNotEmpty();

            harness.passBothPriorities(); // resolve the loss

            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the game"));
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
        });
    }

    @Test
    @DisplayName("Platinum Angel keeps you from losing at the extra turn's end step")
    void platinumAngelPreventsLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new PlatinumAngel());
            castFinalFortune();

            advanceTurn();

            advanceToEndStep();
            harness.passBothPriorities(); // resolve the loss trigger

            // Can't-lose: the trigger resolves but the player stays in the game.
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("Skipping the gained extra turn prevents the delayed loss")
    void skippingGainedExtraTurnPreventsLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new UginsNexus());
            castFinalFortune();

            advanceTurn();

            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.extraTurns).isEmpty();
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
            assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        });
    }

    @Test
    @DisplayName("Casting during an end step still grants an extra turn with its own loss")
    void castingDuringEndStepSchedulesLossForExtraTurn() {
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.END_STEP);
            harness.castFromHand(player1, new FinalFortune(), "{R}{R}");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());

            advanceToEndStep();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("Another Final Fortune during the extra turn does not postpone its loss")
    void anotherFinalFortuneDoesNotPostponeCurrentExtraTurnsLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            castFinalFortune();
            advanceTurn();

            harness.castFromHand(player1, new FinalFortune(), "{R}{R}");
            harness.passBothPriorities();
            assertThat(gd.extraTurns).containsExactly(player1.getId());

            advanceToEndStep();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("Each delayed loss waits for the extra turn that created it")
    void delayedLossesTrackTheirOwnExtraTurns() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new PlatinumAngel());
            harness.setHand(player1, List.of(new FinalFortune(), new FinalFortune()));
            harness.addMana(player1, ManaColor.RED, 4);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();
            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(2);

            advanceTurn();
            advanceToEndStep();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
            harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
            assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

            advanceTurn();
            advanceToEndStep();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
            harness.passBothPriorities();
            assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        });
    }
}
