package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksandTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Quicksand has correct card properties")
    void hasCorrectProperties() {
        Quicksand card = new Quicksand();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        var manaAbility = card.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.isNeedsTarget()).isFalse();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        var sacrificeAbility = card.getActivatedAbilities().get(1);
        assertThat(sacrificeAbility.isRequiresTap()).isTrue();
        assertThat(sacrificeAbility.getManaCost()).isNull();
        assertThat(sacrificeAbility.isNeedsTarget()).isTrue();
        assertThat(sacrificeAbility.getTargetFilter()).isNotNull();
        assertThat(sacrificeAbility.getEffects()).hasSize(2);
        assertThat(sacrificeAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(sacrificeAbility.getEffects().get(1)).isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) sacrificeAbility.getEffects().get(1);
        assertThat(effect.powerBoost()).isEqualTo(-1);
        assertThat(effect.toughnessBoost()).isEqualTo(-2);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new Quicksand());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Sacrifice ability targets attacking creature without flying and gives -1/-2")
    void sacrificeAbilityGivesMinusOneMinusTwo() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        // Quicksand should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Quicksand"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Quicksand"));

        // Attacker should have -1/-2
        assertThat(attacker.getPowerModifier()).isEqualTo(-1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(-2);
        assertThat(attacker.getEffectivePower()).isEqualTo(1);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrifice ability puts ability on the stack (not a mana ability)")
    void sacrificeAbilityUsesStack() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Ability should be on the stack before resolution
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Quicksand");
    }

    @Test
    @DisplayName("Quicksand is sacrificed immediately as a cost, before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        // Before resolution, Quicksand should already be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Quicksand"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Quicksand"));
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetAttackingCreatureWithFlying() {
        harness.addToBattlefield(player1, new Quicksand());
        Permanent flyer = new Permanent(new CloudElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

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
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

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
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent attacker = new Permanent(bigCreature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

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
