package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.cards.s.SimianSling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingRaiju.class, GrizzlyBears.class, BearerOfMemory.class,
        ShortCircuit.class, SimianSling.class})
class ThunderingRaijuTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on a creature you control and damages each opponent")
    void attackTriggerCountersTargetAndDealsDamageForOtherModifiedCreatures() {
        harness.setLife(player2, 20);

        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        raiju.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherModifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        otherModifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void targetingRaijuDoesNotCountItAndDoesNotDamageItsController() {
        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());

        resolveAttackTrigger(raiju);

        assertThat(raiju.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void newlyModifiedTargetCountsButOpposingAndUnmodifiedCreaturesDoNot() {
        addCreatureReady(player1, new ThunderingRaiju());
        Permanent target = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player1, new BearerOfMemory());
        Permanent opposing = addCreatureReady(player2, new BearerOfMemory());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveAttackTrigger(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    void countsEachModifiedCreatureOnceRegardlessOfCounterTypeOrNumber() {
        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        Permanent modified = addCreatureReady(player1, new BearerOfMemory());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        modified.setCounterCount(CounterType.CHARGE, 2);
        Permanent chargeOnly = addCreatureReady(player1, new BearerOfMemory());
        chargeOnly.setCounterCount(CounterType.CHARGE, 1);

        resolveAttackTrigger(raiju);

        harness.assertLife(player2, 18);
    }

    @Test
    void countsOwnAurasAndOpposingEquipmentButNotOpposingAurasOrAttachedEquipmentAsCreatures() {
        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        Permanent ownAuraHost = addCreatureReady(player1, new BearerOfMemory());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        ownAura.setAttachedTo(ownAuraHost.getId());
        Permanent opposingAuraHost = addCreatureReady(player1, new BearerOfMemory());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        opposingAura.setAttachedTo(opposingAuraHost.getId());
        Permanent equipmentHost = addCreatureReady(player1, new BearerOfMemory());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new SimianSling());
        opposingEquipment.setAttachedTo(equipmentHost.getId());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new SimianSling());
        ownEquipment.setAttachedTo(ownAuraHost.getId());
        ownEquipment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveAttackTrigger(raiju);

        harness.assertLife(player2, 18);
    }

    @Test
    void illegalOnlyTargetPreventsTheEntireAbilityIncludingDamage() {
        addCreatureReady(player1, new ThunderingRaiju());
        Permanent target = addCreatureReady(player1, new BearerOfMemory());
        Permanent modified = addCreatureReady(player1, new BearerOfMemory());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
            harness.passBothPriorities();
        });

        harness.assertLife(player2, 20);
    }

    @Test
    void abilityStillResolvesWhenRaijuLeavesTheBattlefield() {
        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        Permanent target = addCreatureReady(player1, new BearerOfMemory());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, raiju);
            harness.passBothPriorities();
        });

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    void modifiedCreatureCountUsesTheBattlefieldAtResolution() {
        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        Permanent removed = addCreatureReady(player1, new BearerOfMemory());
        removed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent newlyModified = addCreatureReady(player1, new BearerOfMemory());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, raiju.getId());
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed);
            newlyModified.setCounterCount(CounterType.CHARGE, 1);
            harness.passBothPriorities();
        });

        harness.assertLife(player2, 19);
    }

    private void resolveAttackTrigger(Permanent target) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });
    }
}
