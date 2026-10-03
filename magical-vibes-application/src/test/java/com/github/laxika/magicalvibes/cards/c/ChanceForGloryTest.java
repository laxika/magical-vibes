package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChanceForGlory.class, GrizzlyBears.class, PlatinumAngel.class})
class ChanceForGloryTest extends BaseCardTest {

    private void castChanceForGlory() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ChanceForGlory(), "{1}{R}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures you control gain indestructible indefinitely")
    void creaturesYouControlGainIndestructible() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChanceForGlory();

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(ownBears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposingBears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Resolving queues an extra turn and delayed loss")
    void resolvingQueuesExtraTurnAndDelayedLoss() {
        int turnBefore = gd.turnNumber;

        castChanceForGlory();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        List<LoseGameAtEndStep> pending = gd.getDelayedActions(LoseGameAtEndStep.class);
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(pending.getFirst().registeredTurnNumber()).isEqualTo(turnBefore);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("You lose at the extra turn's end step")
    void extraTurnEndStepCausesLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            castChanceForGlory();

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());

            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            gs.advanceStep(gd);
            harness.passBothPriorities();

            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("loses the game"));
        });
    }

    @Test
    @DisplayName("Platinum Angel prevents the delayed loss")
    void platinumAngelPreventsLoss() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new PlatinumAngel());
            castChanceForGlory();

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            gs.advanceStep(gd);
            harness.passBothPriorities();

            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain indestructible")
    void creaturesEnteringLaterDoNotGainIndestructible() {
        castChanceForGlory();

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible persists beyond the extra turn when the loss is prevented")
    void indestructiblePersistsBeyondExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new PlatinumAngel());
            castChanceForGlory();
            Permanent bears = findPermanent(player1, "Grizzly Bears");

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.passUntil(player1, TurnStep.END_STEP);
            harness.passBothPriorities();
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
            harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

            assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        });
    }

    @Test
    @DisplayName("Casting on an opponent's turn grants the caster an extra turn with its own delayed loss")
    void castingOnOpponentsTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.castFromHand(player1, new ChanceForGlory(), "{1}{R}{W}");
            harness.passBothPriorities();

            harness.passUntil(player2, TurnStep.END_STEP);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
            harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
            assertThat(gd.currentTurnIsExtraTurn).isTrue();
            harness.passUntil(player1, TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
            harness.passBothPriorities();

            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        });
    }

    @Test
    @DisplayName("An extra turn added later does not trigger an earlier Chance for Glory's loss")
    void delayedLossIsBoundToItsSpecificExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new PlatinumAngel());
            castChanceForGlory();
            harness.castFromHand(player2, new ChanceForGlory(), "{1}{R}{W}");
            harness.passBothPriorities();

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            harness.passUntil(player2, TurnStep.END_STEP);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
            assertThat(gd.getDelayedActions(LoseGameAtEndStep.class).getFirst().playerId())
                    .isEqualTo(player1.getId());
            harness.passBothPriorities();
            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        });
    }
}
