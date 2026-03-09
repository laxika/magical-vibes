package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasCoreAugur;
import com.github.laxika.magicalvibes.cards.v.VensersJournal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StepTriggerServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("handleDrawStep")
    class HandleDrawStep {

        @Test
        @DisplayName("Starting player skips draw on turn 1")
        void startingPlayerSkipsDrawOnTurn1() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);
            gd.turnNumber = 1;
            gd.startingPlayerId = player1.getId();
            List<Card> hand = new ArrayList<>(gd.playerHands.get(player1.getId()));
            int handSizeBefore = hand.size();

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleDrawStep(gd);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        }

        @Test
        @DisplayName("Active player draws a card on turn 2+")
        void activePlayerDrawsCardOnTurn2() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);
            gd.turnNumber = 2;
            int handSizeBefore = gd.playerHands.get(player1.getId()).size();

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleDrawStep(gd);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        }

        @Test
        @DisplayName("Non-starting player draws on turn 1")
        void nonStartingPlayerDrawsOnTurn1() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.UPKEEP);
            gd.turnNumber = 1;
            gd.startingPlayerId = player1.getId();
            int handSizeBefore = gd.playerHands.get(player2.getId()).size();

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleDrawStep(gd);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
        }
    }

    @Nested
    @DisplayName("handleUpkeepTriggers")
    class HandleUpkeepTriggers {

        @Test
        @DisplayName("Venser's Journal pushes upkeep trigger onto stack")
        void vensersJournalPushesUpkeepTrigger() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UNTAP);
            gd.turnNumber = 2;
            harness.addToBattlefield(player1, new VensersJournal());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Venser's Journal");
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with upkeep effects")
        void noTriggersWhenNoPermanentsWithUpkeepEffects() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UNTAP);
            gd.turnNumber = 2;
            harness.addToBattlefield(player1, new GrizzlyBears());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opponent's upkeep-triggered permanent does not trigger during active player's upkeep")
        void opponentUpkeepTriggeredDoesNotFireForActivePlayer() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UNTAP);
            gd.turnNumber = 2;
            // Venser's Journal has UPKEEP_TRIGGERED (controller's upkeep only)
            harness.addToBattlefield(player2, new VensersJournal());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleEndStepTriggers")
    class HandleEndStepTriggers {

        @Test
        @DisplayName("Jin-Gitaxias pushes controller end-step trigger onto stack")
        void jinGitaxiasPushesControllerEndStepTrigger() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Jin-Gitaxias, Core Augur");
        }

        @Test
        @DisplayName("Jin-Gitaxias does not trigger during opponent's end step")
        void jinGitaxiasDoesNotTriggerDuringOpponentEndStep() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with end-step effects")
        void noTriggersWhenNoPermanentsWithEndStepEffects() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.addToBattlefield(player1, new GrizzlyBears());

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handlePrecombatMainTriggers")
    class HandlePrecombatMainTriggers {

        @Test
        @DisplayName("Does nothing when there are no opening hand mana triggers")
        void doesNothingWithNoOpeningHandTriggers() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DRAW);

            StepTriggerService svc = new StepTriggerService(
                    harness.getDrawService(), gqs, harness.getGameBroadcastService(),
                    harness.getPlayerInputService(), harness.getPermanentRemovalService(),
                    harness.getBattlefieldEntryService());
            svc.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }
}
