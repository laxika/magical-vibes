package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieldSurgeon.class, GrizzlyBears.class, ProdigalPyromancer.class, Forest.class})
class FieldSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature gives the target creature a one-damage prevention shield")
    void preventsNextDamageToTargetCreature() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("May tap itself to pay the cost and target a creature it controls")
    void canTapItselfToPayCostAndTargetOwnCreature() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();

        assertThat(surgeon.isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap")
    void requiresUntappedCreatureToTap() {
        Permanent surgeon = addSurgeonWithCostCreature();
        findPermanent(player1, "Grizzly Bears").tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, surgeon), null, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A summoning-sick Field Surgeon can tap itself and target itself")
    void summoningSickSurgeonCanTapAndProtectItself() {
        Permanent surgeon = harness.addToBattlefieldAndReturn(player1, new FieldSurgeon());
        surgeon.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(player1, surgeon), null, surgeon.getId());
        harness.passBothPriorities();

        assertThat(surgeon.isTapped()).isTrue();
        assertThat(surgeon.getDamagePreventionShield()).isEqualTo(1);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, surgeon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Field Surgeon");
        assertThat(surgeon.getMarkedDamage()).isZero();
        assertThat(surgeon.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the tap cost")
    void summoningSickSupportCreatureCanPayCost() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        surgeon.tap();
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        costCreature.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();

        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations choose separate cost creatures and accumulate prevention")
    void canChooseCostCreatureAndActivateAgainWhileTapped() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent firstCost = findPermanent(player1, "Grizzly Bears");
        Permanent secondCost = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());
        assertThat(secondCost.isTapped()).isTrue();
        assertThat(firstCost.isTapped()).isFalse();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();
        assertThat(firstCost.isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);

        Permanent firstPyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent secondPyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, indexOf(player2, firstPyromancer), null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        harness.activateAbility(player2, indexOf(player2, secondPyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only the next point of damage is prevented")
    void laterDamageIsNotPreventedAfterShieldIsUsed() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstPyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent secondPyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, firstPyromancer), null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, secondPyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("An unused shield expires at end of turn")
    void unusedShieldExpiresAtEndOfTurn() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing creatures and untapped lands cannot pay the cost")
    void cannotPayWithOpposingCreatureOrLand() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        surgeon.tap();
        harness.addToBattlefield(player1, new Forest());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only one point is prevented from a larger combat damage event")
    void preventsOnePointOfCombatDamage() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent costCreature = findPermanent(player1, "Grizzly Bears");
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, blocker.getId());
        harness.handlePermanentChosen(player1, costCreature.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));
        gs.declareBlockers(gd, player2, java.util.Map.of(indexOf(player2, blocker), List.of(indexOf(player1, attacker))));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getDamagePreventionShield()).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addSurgeonWithCostCreature() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        surgeon.tap();
        addCreatureReady(player1, new GrizzlyBears());
        return surgeon;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
