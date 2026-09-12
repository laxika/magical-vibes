package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KilnspireDistrict.class, GrizzlyBears.class})
class KilnspireDistrictTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new KilnspireDistrict(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkToAndFirstMainPhaseAddChargeCounterThenRedMana() {
        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.CHARGE, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handlePrecombatMainTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.CHARGE, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void chaosMayPayXToDealXDamageToAnyTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

        harness.handleXValueChosen(player1, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningChaosPaymentDealsNoDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
