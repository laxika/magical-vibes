package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenWizardsMountain.class, BurnishedHart.class})
class TenWizardsMountainTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = harness.getTriggerCollectionService();
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TenWizardsMountain(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planarDieRollPutsACounterOnUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        rollBlankAndChooseTarget(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void planarDieRollCanBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        harness.inMutationScope(() -> planar.completeRoll(gd, player1.getId(), PlanarDieResult.BLANK));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chaosGivesYourCreaturesFlyingUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void eachPlanarDieRollAddsAnotherCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        rollBlankAndChooseTarget(target.getId());
        rollBlankAndChooseTarget(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void rollingAnOrdinaryDieDoesNotTriggerThePlane() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        harness.inMutationScope(() -> triggers.checkControllerRollsOneOrMoreDiceTriggers(
                gd, player1.getId(), 1, 20));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void planarDieRollWithNoCreaturesCanChooseNoTarget() {
        harness.inMutationScope(() -> planar.completeRoll(gd, player1.getId(), PlanarDieResult.BLANK));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosAffectsCreaturesPresentAtResolutionOnly() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.inMutationScope(() -> planar.chaos(gd));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FLYING)).isFalse();
    }

    @Test
    void chaosUsesTheCurrentPlanarController() {
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    private void rollBlankAndChooseTarget(java.util.UUID targetId) {
        harness.inMutationScope(() -> planar.completeRoll(gd, player1.getId(), PlanarDieResult.BLANK));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
