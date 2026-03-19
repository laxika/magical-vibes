package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfantryVeteranTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Infantry Veteran has tap ability with BoostTargetCreatureEffect targeting attacking creatures")
    void hasCorrectProperties() {
        InfantryVeteran card = new InfantryVeteran();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter()).isNotNull();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
    }

    // ===== Activation on attacking creature =====

    @Test
    @DisplayName("Activating ability on attacking creature puts it on the stack")
    void activatingOnAttackingCreaturePutsOnStack() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Infantry Veteran");
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability gives attacking creature +1/+1")
    void resolvingBoostsAttackingCreature() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Activating ability taps Infantry Veteran")
    void activatingTapsVeteran() {
        Permanent veteran = addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(veteran.isTapped()).isTrue();
    }

    // ===== Target restriction: must be attacking =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addReadyVeteran(player1);
        Permanent nonAttacker = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    // ===== Can target opponent's attacking creature =====

    @Test
    @DisplayName("Can target opponent's attacking creature")
    void canTargetOpponentAttackingCreature() {
        addReadyVeteran(player1);
        Permanent opponentAttacker = addAttackingCreature(player2);

        harness.activateAbility(player1, 0, null, opponentAttacker.getId());
        harness.passBothPriorities();

        assertThat(opponentAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(opponentAttacker.getToughnessModifier()).isEqualTo(1);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyVeteran(Player player) {
        Permanent perm = new Permanent(new InfantryVeteran());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
