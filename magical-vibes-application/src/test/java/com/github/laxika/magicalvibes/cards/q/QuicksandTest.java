package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quicksand.class, GrizzlyBears.class, AirElemental.class, HillGiant.class})
class QuicksandTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Sacrifice ability targets attacking creature without flying and gives -1/-2")
    void sacrificeAbilityGivesMinusOneMinusTwo() {
        harness.addToBattlefield(player1, new Quicksand());
        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(4);
        creature.setToughness(4);
        Permanent attacker = addCreatureReady(player2, creature);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        // Quicksand should be sacrificed
        harness.assertNotOnBattlefield(player1, "Quicksand");
        harness.assertInGraveyard(player1, "Quicksand");

        // Attacker should have -1/-2
        assertThat(attacker.getPowerModifier()).isEqualTo(-1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(-2);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice ability puts ability on the stack (not a mana ability)")
    void sacrificeAbilityUsesStack() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Ability should be on the stack before resolution
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Quicksand is sacrificed immediately as a cost, before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Before resolution, Quicksand should already be sacrificed
        harness.assertNotOnBattlefield(player1, "Quicksand");
        harness.assertInGraveyard(player1, "Quicksand");
    }

    @Test
    @DisplayName("Sacrifice ability fizzles if the target stops attacking before resolution")
    void sacrificeAbilityFizzlesIfTargetStopsAttackingBeforeResolution() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrifice ability can target an attacking creature you control")
    void sacrificeAbilityCanTargetOwnAttackingCreature() {
        harness.addToBattlefield(player1, new Quicksand());
        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(4);
        creature.setToughness(4);
        Permanent attacker = addCreatureReady(player1, creature);

        declareAttackersAndPrepareBlockers(List.of(1));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quicksand");
        assertThat(attacker.getPowerModifier()).isEqualTo(-1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(-2);
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetAttackingCreatureWithFlying() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent flyer = addCreatureReady(player2, new AirElemental());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature gaining flying before resolution is no longer a legal target")
    void gainingFlyingBeforeResolutionPreventsDebuff() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        attacker.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quicksand");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reducing an indestructible attacker's toughness to zero puts it in the graveyard")
    void zeroToughnessKillsIndestructibleAttacker() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Quicksand");
    }

    @Test
    @DisplayName("The debuff remains after its target stops attacking once the ability has resolved")
    void debuffRemainsAfterAttackerLeavesCombat() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(false);

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Debuff wears off =====

    @Test
    @DisplayName("-1/-2 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Quicksand());
        // Use a 4/4 so it survives the -1/-2 debuff (becomes 3/2)
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent attacker = addCreatureReady(player2, bigCreature);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(-1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(-2);

        // Advance to cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
    }
}
