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
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void chaosRevealsTopPlanarCardAndAcceptingPutsItOnBottom() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.planechase.deck).containsExactly(revealed, belowRevealed);
        assertThat(gameLogContains("The top card of the planar deck is revealed: Panopticon.")).isTrue();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.planechase.deck).containsExactly(belowRevealed, revealed);
    }

    @Test
    void chaosLeavesRevealedPlanarCardOnTopWhenDeclined() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
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

    @Test
    void planarRollDrawsForTheNewActivePlayerOnly() {
        harness.forceActivePlayer(player2);
        Card drawn = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn, new Forest()));
        int otherHandSize = gd.playerHands.get(player1.getId()).size();
        when(die.roll()).thenReturn(com.github.laxika.magicalvibes.model.planar.PlanarDieResult.BLANK);

        harness.inMutationScope(() -> planar.roll(gd, player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(otherHandSize);
    }

    @Test
    void rollingAnOrdinaryDieDoesNotDrawACard() {
        int before = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(TriggerCollectionService.class)
                .checkControllerRollsOneOrMoreDiceTriggers(gd, player1.getId(), 1, 6));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }

    @Test
    void chaosWithAnEmptyPlanarDeckDoesNotOfferAChoice() {
        gd.planechase.deck.clear();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.planechase.deck).isEmpty();
    }

    @Test
    void chaosCanPutTheOnlyPlanarCardOnTheBottom() {
        gd.planechase.deck.removeLast();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.planechase.deck).containsExactly(revealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void faceUpPlaneAlsoRemovesMaximumHandSizeForItsController() {
        harness.setHand(player1, java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> (Card) new Forest()).toList());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.cleanupDiscardPending).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    void maximumHandSizeReturnsWhenThePlaneIsNoLongerFaceUp() {
        harness.setHand(player1, java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> (Card) new Forest()).toList());
        gd.planechase.faceUp.clear();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.cleanupDiscardPending).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }
}
