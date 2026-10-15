package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.s.SpatialMerging;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({NornsSeedcore.class, Panopticon.class, SpatialMerging.class})
class NornsSeedcoreTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new NornsSeedcore(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void chaosAddsTheFirstRevealedPlaneWithoutLeavingExistingPlanes() {
        Card phenomenon = new Card();
        phenomenon.setName("Test phenomenon");
        phenomenon.setType(CardType.PHENOMENON);
        Panopticon arrivingPlane = new Panopticon();
        Card secondPhenomenon = new Card();
        secondPhenomenon.setName("Second test phenomenon");
        secondPhenomenon.setType(CardType.PHENOMENON);
        Card existingPlane = gd.planechase.faceUp.getFirst().getCard();
        gd.planechase.deck.addAll(List.of(phenomenon, secondPhenomenon, arrivingPlane));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        PendingInteraction.PlanarDeckPlaneswalkCardOrder choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarDeckPlaneswalkCardOrder.class);
        assertThat(choice).isNotNull();
        assertThat(choice.arrivingPlane()).isSameAs(arrivingPlane);
        assertThat(choice.cardsToBottom()).containsExactly(phenomenon, secondPhenomenon);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(existingPlane, arrivingPlane);
        assertThat(gd.planechase.deck).containsExactly(secondPhenomenon, phenomenon);
    }

    @Test
    void planeswalkingToSeedcoreCausesChaos() {
        gd.planechase.faceUp.clear();
        Card existingPlane = new Card();
        existingPlane.setName("Existing plane");
        existingPlane.setType(CardType.PLANE);
        gd.planechase.faceUp.add(new PlanarObject(existingPlane, gd.nextTimestamp()));
        NornsSeedcore seedcore = new NornsSeedcore();
        Card arrivingPlane = new Card();
        arrivingPlane.setName("Arriving plane");
        arrivingPlane.setType(CardType.PLANE);
        gd.planechase.deck.addAll(List.of(seedcore, arrivingPlane));

        harness.inMutationScope(() -> planar.reveal(gd, true));
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(existingPlane, seedcore, arrivingPlane);
    }

    @Test
    void skipsPhenomenonAndStopsAtFirstPlaneThenTriggersItsArrivalAbility() {
        SpatialMerging phenomenon = new SpatialMerging();
        Panopticon arrivingPlane = new Panopticon();
        Panopticon unrevealedPlane = new Panopticon();
        Card existingPlane = gd.planechase.faceUp.getFirst().getCard();
        gd.planechase.deck.addAll(List.of(phenomenon, arrivingPlane, unrevealedPlane));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(existingPlane, arrivingPlane);
        assertThat(gd.planechase.deck).containsExactly(unrevealedPlane, phenomenon);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noPlaneFoundReturnsAllRevealedPhenomenaInChosenOrder() {
        SpatialMerging first = new SpatialMerging();
        SpatialMerging second = new SpatialMerging();
        Card existingPlane = gd.planechase.faceUp.getFirst().getCard();
        gd.planechase.deck.addAll(List.of(first, second));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        PendingInteraction.PlanarDeckPlaneswalkCardOrder choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarDeckPlaneswalkCardOrder.class);
        assertThat(choice).isNotNull();
        assertThat(choice.arrivingPlane()).isNull();
        assertThat(choice.cardsToBottom()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(existingPlane);
        assertThat(gd.planechase.deck).containsExactly(second, first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosWithEmptyPlanarDeckLeavesExistingPlaneInPlace() {
        Card existingPlane = gd.planechase.faceUp.getFirst().getCard();
        gd.planechase.deck.clear();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(existingPlane);
        assertThat(gd.planechase.deck).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
