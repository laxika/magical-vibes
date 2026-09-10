package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({CliffsideMarket.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CliffsideMarketTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new CliffsideMarket(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkAndUpkeepAbilitiesExchangeLifeTotalsWithTargetPlayer() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 23);

        resolveLifeExchange(EffectSlot.PLANESWALK_TO_TRIGGERED);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 11);

        harness.setLife(player1, 7);
        harness.setLife(player2, 19);
        resolveLifeExchange(EffectSlot.UPKEEP_TRIGGERED);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 7);
    }

    @Test
    void chaosExchangesTwoPermanentsThatShareACardType() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextETBTokenMultiTargetTrigger(gd));

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).contains(first.getId(), second.getId(), land.getId());

        harness.handlePermanentChosen(player1, first.getId());
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validIds()).contains(second.getId()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, first.getId())).isEqualTo(player2.getId());
        assertThat(gqs.findPermanentController(gd, second.getId())).isEqualTo(player1.getId());
    }

    private void resolveLifeExchange(EffectSlot slot) {
        PlanarObject plane = gd.planechase.faceUp.getFirst();
        harness.inMutationScope(() -> planar.trigger(gd, plane, slot, player1.getId()));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
