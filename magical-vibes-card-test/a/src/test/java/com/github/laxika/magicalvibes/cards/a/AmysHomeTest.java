package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmysHome.class, Forest.class, GrizzlyBears.class})
class AmysHomeTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new AmysHome(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToAmyHomeMayExileANonlandCardWithManaValueTimeCounters() {
        Forest forest = new Forest();
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, card));

        PlanarObject plane = gd.planechase.faceUp.getFirst();
        harness.inMutationScope(() -> planar.trigger(
                gd, plane, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    void upkeepMayExileANonlandCardWithManaValueTimeCounters() {
        Forest forest = new Forest();
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, card));

        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    void chaosEnsuesTimeTravelsAcrossSuspendedCardsAndControlledTimeCounters() {
        GrizzlyBears suspendedCard = new GrizzlyBears();
        harness.setExile(player1, List.of(suspendedCard));
        gd.exiledCardTimeCounters.put(suspendedCard.getId(), 2);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        permanent.setCounterCount(CounterType.TIME, 1);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "ADD");

        assertThat(permanent.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.exiledCardTimeCounters).containsEntry(suspendedCard.getId(), 3);
    }
}
