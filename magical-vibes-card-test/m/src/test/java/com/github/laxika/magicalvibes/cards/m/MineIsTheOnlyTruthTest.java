package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.OrzhovSignet;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineIsTheOnlyTruth.class, OrzhovSignet.class})
class MineIsTheOnlyTruthTest extends BaseCardTest {

    @Test
    void controllerDrawsWhenAnyPlayerCastsASpell() {
        addFaceUpScheme();
        harness.setHand(player1, List.of());
        OrzhovSignet drawnCard = new OrzhovSignet();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new OrzhovSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void schemeIsAbandonedAtUpkeepAfterControllerDrewLastTurn() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        gd.cardsDrawnLastTurn.put(player1.getId(), 1);
        beginUpkeepAndCollectTriggers(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player1.getId())).contains(scheme);
        assertThat(gd.faceDownCommandZoneCards).contains(scheme.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme);
    }

    @Test
    void schemeStaysWhenControllerDidNotDrawLastTurn() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        beginUpkeepAndCollectTriggers(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(scheme);
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
    }

    @Test
    void controllerAlsoDrawsForTheirOwnSpell() {
        addFaceUpScheme();
        OrzhovSignet drawnCard = new OrzhovSignet();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new OrzhovSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsUpkeepDoesNotAbandonScheme() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        gd.cardsDrawnLastTurn.put(player1.getId(), 1);

        beginUpkeepAndCollectTriggers(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
    }

    @Test
    void drawingThisTurnDoesNotSatisfyLastTurnCondition() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);

        beginUpkeepAndCollectTriggers(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
    }

    @Test
    void opponentsDrawLastTurnDoesNotSatisfyCondition() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        gd.cardsDrawnLastTurn.put(player2.getId(), 1);

        beginUpkeepAndCollectTriggers(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(scheme.getId());
    }

    @Test
    void faceDownSchemeDoesNotTriggerForSpells() {
        MineIsTheOnlyTruth scheme = addFaceUpScheme();
        gd.faceDownCommandZoneCards.add(scheme.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new OrzhovSignet()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new OrzhovSignet(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private MineIsTheOnlyTruth addFaceUpScheme() {
        MineIsTheOnlyTruth scheme = new MineIsTheOnlyTruth();
        scheme.setOwnerId(player1.getId());
        gd.playerCommandZones.get(player1.getId()).add(scheme);
        return scheme;
    }

    private void beginUpkeepAndCollectTriggers(Player activePlayer) {
        gd.turnNumber = 2;
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
    }
}
