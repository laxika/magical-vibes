package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quicksand.class, Python.class, CloudElemental.class})
class QuicksandTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new Quicksand());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Quicksand");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Sacrifice ability targets attacking creature without flying and gives -1/-2")
    void sacrificeAbilityGivesMinusOneMinusTwo() {
        harness.addToBattlefield(player1, new Quicksand());
        Python creature = new Python();
        creature.setPower(4);
        creature.setToughness(4);
        Permanent attacker = addCreatureReady(player2, creature);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

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
        Permanent attacker = addCreatureReady(player2, new Python());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Ability should be on the stack before resolution
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Quicksand is sacrificed immediately as a cost, before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new Python());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Before resolution, Quicksand should already be sacrificed
        harness.assertNotOnBattlefield(player1, "Quicksand");
        harness.assertInGraveyard(player1, "Quicksand");
    }

    @Test
    @DisplayName("Sacrifice ability fizzles if the target stops attacking before resolution")
    void sacrificeAbilityFizzlesIfTargetStopsAttackingBeforeResolution() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new Python());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent creature = addCreatureReady(player2, new Python());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetAttackingCreatureWithFlying() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent flyer = addCreatureReady(player2, new CloudElemental());
        flyer.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = addCreatureReady(player2, new Python());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

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
        Python bigCreature = new Python();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent attacker = addCreatureReady(player2, bigCreature);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

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
