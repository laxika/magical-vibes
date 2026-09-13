package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.planar.PlanarDieRoller;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CardUsed({StairsToInfinity.class, Panopticon.class, Forest.class})
class StairsToInfinityTest extends BaseCardTest {
    private PlanechaseService planar;
    private PlanarDieRoller die;
    private Card revealed;
    private Card belowRevealed;

    @BeforeEach
    void preparePlane() {
        die = mock(PlanarDieRoller.class);
        planar = new PlanechaseService(die, gqs,
                GameTestEngineContext.get().getBean(GameLogService.class),
                GameTestEngineContext.get().getBean(TriggerCollectionService.class),
                GameTestEngineContext.get().getBean(ConditionEvaluationService.class),
                GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.cards.CardCatalog.class));
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new StairsToInfinity(), gd.nextTimestamp()));
        revealed = new Panopticon();
        belowRevealed = new Panopticon();
        gd.planechase.deck.addAll(List.of(revealed, belowRevealed));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planarDieRollDrawsACard() {
        int before = gd.playerHands.get(player1.getId()).size();

        when(die.roll()).thenReturn(com.github.laxika.magicalvibes.model.planar.PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void chaosRevealsTopPlanarCardAndAcceptingPutsItOnBottom() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.planechase.deck).containsExactly(revealed, belowRevealed);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.planechase.deck).containsExactly(belowRevealed, revealed);
    }

    @Test
    void chaosLeavesRevealedPlanarCardOnTopWhenDeclined() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.planechase.deck).containsExactly(revealed, belowRevealed);
    }

    @Test
    void faceUpPlaneRemovesMaximumHandSizeForNonController() {
        List<Card> oversizedHand = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            oversizedHand.add(new Forest());
        }
        harness.setHand(player2, oversizedHand);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.cleanupDiscardPending).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
    }
}
