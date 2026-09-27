package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineIsTheOnlyTruth.class, GrizzlyBears.class})
class MineIsTheOnlyTruthTest extends BaseCardTest {

    @Test
    void controllerDrawsWhenAnyPlayerCastsASpell() {
        harness.addToBattlefield(player1, new MineIsTheOnlyTruth());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void schemeIsAbandonedAtUpkeepAfterControllerDrewLastTurn() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MineIsTheOnlyTruth());
        gd.cardsDrawnLastTurn.put(player1.getId(), 1);
        beginUpkeepAndCollectTriggers();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scheme.getCard());
    }

    @Test
    void schemeStaysWhenControllerDidNotDrawLastTurn() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MineIsTheOnlyTruth());
        beginUpkeepAndCollectTriggers();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    private void beginUpkeepAndCollectTriggers() {
        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
    }
}
