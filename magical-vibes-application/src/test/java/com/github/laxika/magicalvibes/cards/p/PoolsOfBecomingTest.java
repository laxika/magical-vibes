package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoolsOfBecoming.class, Panopticon.class, Forest.class})
class PoolsOfBecomingTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new PoolsOfBecoming(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void endStepExchangesHandForCardsFromLibrary() {
        List<Card> hand = List.of(new Forest(), new Forest());
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);

        harness.inMutationScope(() -> planar.step(gd, EffectSlot.CONTROLLER_END_STEP_TRIGGERED));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hand.get(1), hand.get(0));
    }

    @Test
    void chaosRevealsThreePlanarCardsTriggersTheirChaosAbilitiesAndBottomsThemInOrder() {
        List<Card> revealed = List.of(new Panopticon(), new Panopticon(), new Panopticon());
        gd.planechase.deck.addAll(revealed);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.planarDeck()).isTrue();
        assertThat(reorder.cards()).containsExactlyElementsOf(revealed);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));
        assertThat(gd.planechase.deck).containsExactly(revealed.get(2), revealed.get(1), revealed.get(0));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
    }
}
