package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GargoyleFlock;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HierophantBioTitan.class, GrizzlyBears.class, GargoyleFlock.class, GiantGrowth.class})
class HierophantBioTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Removes counters from controlled creatures to reduce its cast cost")
    void removesCountersForCostReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreatureWithCounterCostReduction(player1, 0, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hierophant Bio-Titan");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost reduction is optional")
    void mayCastWithoutRemovingCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void canRemoveSomeCountersFromMultipleCreaturesAsAnImmediateCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(first.getId(), second.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void mayRemoveMoreCountersThanNeededToEliminateGenericCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void rejectsRemovingCountersFromAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HierophantBioTitan());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        harness.assertInHand(player1, "Hierophant Bio-Titan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsRemovingMoreCountersThanTheCreatureHas() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId(), creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        harness.assertInHand(player1, "Hierophant Bio-Titan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersOtherThanPlusOnePlusOneCannotPayTheCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.CHARGE)).isOne();
        harness.assertInHand(player1, "Hierophant Bio-Titan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void failedManaPaymentRestoresCountersRemovedForTheCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId(), creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Hierophant Bio-Titan");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
    }

    @Test
    void counterReductionCannotReplaceTheTwoGreenMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        harness.setHand(player1, List.of(new HierophantBioTitan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithCounterCostReduction(player1, 0,
                List.of(creature.getId(), creature.getId(), creature.getId(),
                        creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertInHand(player1, "Hierophant Bio-Titan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void titanicRejectsABlockerWithPowerExactlyTwo() {
        Permanent attacker = addCreatureReady(player1, new HierophantBioTitan());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void titanicRejectsABlockerWithPowerBelowTwo() {
        Permanent attacker = addCreatureReady(player1, new HierophantBioTitan());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void titanicUsesTheBlockersEffectivePowerIncludingCounters() {
        Permanent attacker = addCreatureReady(player1, new HierophantBioTitan());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceKeepsItUntappedWhenDeclaredAsAnAttacker() {
        Permanent titan = addCreatureReady(player1, new HierophantBioTitan());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(titan.isAttacking()).isTrue();
        assertThat(titan.isTapped()).isFalse();
    }

    @Test
    void reachLetsItBlockAFlyingCreatureWithPowerTwo() {
        Permanent attacker = addCreatureReady(player2, new GargoyleFlock());
        Permanent titan = addCreatureReady(player1, new HierophantBioTitan());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(titan.isBlocking()).isTrue();
    }

    @Test
    void wardCountersAnOpponentsSpellWhenPaymentIsDeclined() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, titan.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(12);
        harness.assertInGraveyard(player2, "Giant Growth");
    }

    @Test
    void payingWardConsumesTwoManaAndLetsTheSpellResolve() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, titan.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(15);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new HierophantBioTitan());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, titan.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(15);
    }
}
