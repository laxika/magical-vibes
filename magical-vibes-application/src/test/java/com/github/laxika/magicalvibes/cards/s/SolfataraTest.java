package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolfataraTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Solfatara()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private List<Integer> player2Playable() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());
    }

    @Test
    @DisplayName("Target can't play lands this turn but can still cast creatures")
    void blocksLandsButNotCreatures() {
        castAtPlayer2();

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0);
        assertThat(playable).contains(1);
    }

    @Test
    @DisplayName("Land restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAtPlayer2();
        assertThat(player2Playable()).doesNotContain(0);

        TurnCleanupService turnCleanupService = GameTestEngineContext.get().getBean(TurnCleanupService.class);
        harness.inMutationScope(() -> turnCleanupService.applyCleanupResets(gd));

        assertThat(player2Playable()).contains(0);
    }

    @Test
    @DisplayName("Schedules a draw for the controller at the next turn's upkeep")
    void schedulesDelayedDraw() {
        castAtPlayer2();
        GameData gd = harness.getGameData();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        castAtPlayer2();
        GameData gd = harness.getGameData();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
