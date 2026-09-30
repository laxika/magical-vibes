package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cat;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCheetahPlanet.class, Cat.class, FugitiveWizard.class, GrizzlyBears.class})
class TheCheetahPlanetTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheCheetahPlanet(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkAndUpkeepPutCountersOnAndMakeOnlyEligibleCreatureACat() {
        Permanent planeswalkTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent upkeepTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent cat = addCreatureReady(player1, new Cat());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        triggerAndChooseTarget(EffectSlot.PLANESWALK_TO_TRIGGERED, planeswalkTarget);

        assertThat(planeswalkTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, planeswalkTarget, CardSubtype.CAT)).isTrue();

        harness.forceStep(TurnStep.UPKEEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(upkeepTarget.getId())
                .doesNotContain(planeswalkTarget.getId(), cat.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, upkeepTarget.getId());
        harness.passBothPriorities();

        assertThat(upkeepTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chaosGivesCatsProvokeForTheTurn() {
        Permanent cat = addCreatureReady(player1, new Cat());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.tap();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMustBlockIds()).containsExactly(cat.getId());
    }

    private void triggerAndChooseTarget(EffectSlot slot, Permanent target) {
        PlanarObject plane = gd.planechase.faceUp.getFirst();
        harness.inMutationScope(() -> planar.trigger(gd, plane, slot, player1.getId()));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
