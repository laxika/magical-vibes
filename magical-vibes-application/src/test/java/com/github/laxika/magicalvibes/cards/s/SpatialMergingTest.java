package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
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

@CardUsed({SpatialMerging.class, Panopticon.class})
class SpatialMergingTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalksToBothRevealedPlanesAndOrdersOtherCards() {
        SpatialMerging source = new SpatialMerging();
        Panopticon firstPlane = new Panopticon();
        Panopticon secondPlane = new Panopticon();
        SpatialMerging firstOther = new SpatialMerging();
        SpatialMerging secondOther = new SpatialMerging();
        gd.planechase.faceUp.add(new PlanarObject(source, gd.nextTimestamp()));
        gd.planechase.deck.addAll(List.of(firstPlane, firstOther, secondOther, secondPlane));

        harness.inMutationScope(() -> planar.trigger(gd, gd.planechase.faceUp.getFirst(),
                EffectSlot.ENCOUNTER_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        PendingInteraction.SpatialMergingCardOrder choice =
                gd.interaction.activeInteraction(PendingInteraction.SpatialMergingCardOrder.class);
        assertThat(choice).isNotNull();
        assertThat(choice.planes()).containsExactly(firstPlane, secondPlane);
        assertThat(choice.cardsToBottom()).containsExactly(firstOther, secondOther);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(firstPlane, secondPlane);
        assertThat(gd.planechase.deck).containsExactly(source, secondOther, firstOther);
    }

    @Test
    void planeswalksImmediatelyWhenNoOtherCardsAreRevealed() {
        SpatialMerging source = new SpatialMerging();
        Panopticon firstPlane = new Panopticon();
        Panopticon secondPlane = new Panopticon();
        gd.planechase.faceUp.add(new PlanarObject(source, gd.nextTimestamp()));
        gd.planechase.deck.addAll(List.of(firstPlane, secondPlane));

        harness.inMutationScope(() -> planar.trigger(gd, gd.planechase.faceUp.getFirst(),
                EffectSlot.ENCOUNTER_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard)
                .containsExactly(firstPlane, secondPlane);
        assertThat(gd.planechase.deck).containsExactly(source);
    }
}
