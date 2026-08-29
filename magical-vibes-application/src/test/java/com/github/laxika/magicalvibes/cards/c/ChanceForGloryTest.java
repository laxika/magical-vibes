package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class ChanceForGloryTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void castChanceForGlory() {
        harness.setHand(player1, List.of(new ChanceForGlory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures you control gain indestructible until end of turn")
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

        assertThat(ownBears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
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
        enableAutoStop();
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
    }

    @Test
    @DisplayName("Platinum Angel prevents the delayed loss")
    void platinumAngelPreventsLoss() {
        enableAutoStop();
        harness.addToBattlefield(player1, new PlatinumAngel());
        castChanceForGlory();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
