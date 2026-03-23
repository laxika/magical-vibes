package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuskborneSkymarcharTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Duskborne Skymarcher has tap+mana ability with BoostTargetCreatureEffect targeting attacking Vampires")
    void hasCorrectProperties() {
        DuskborneSkymarcher card = new DuskborneSkymarcher();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{W}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter()).isNotNull();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) card.getActivatedAbilities().get(0).getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentAllOfPredicate.class);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
    }

    // ===== Activation on attacking Vampire =====

    @Test
    @DisplayName("Activating ability on attacking Vampire puts it on the stack")
    void activatingOnAttackingVampirePutsOnStack() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Duskborne Skymarcher");
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability gives attacking Vampire +1/+1")
    void resolvingBoostsAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Activating ability taps Duskborne Skymarcher")
    void activatingTapsSkymarcher() {
        Permanent skymarcher = addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(skymarcher.isTapped()).isTrue();
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Cannot activate without paying {W}")
    void cannotActivateWithoutMana() {
        addReadySkymarcher(player1);
        Permanent attacker = addAttackingVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Target restriction: must be attacking Vampire =====

    @Test
    @DisplayName("Cannot target a non-attacking Vampire")
    void cannotTargetNonAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent nonAttacker = addReadyVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Vampire");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Vampire creature")
    void cannotTargetAttackingNonVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingNonVampire(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Vampire");
    }

    // ===== Can target opponent's attacking Vampire =====

    @Test
    @DisplayName("Can target opponent's attacking Vampire")
    void canTargetOpponentAttackingVampire() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent opponentAttacker = addAttackingVampire(player2);

        harness.activateAbility(player1, 0, null, opponentAttacker.getId());
        harness.passBothPriorities();

        assertThat(opponentAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(opponentAttacker.getToughnessModifier()).isEqualTo(1);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadySkymarcher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent attacker = addAttackingVampire(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadySkymarcher(Player player) {
        Permanent perm = new Permanent(new DuskborneSkymarcher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingVampire(Player player) {
        Permanent perm = new Permanent(new VampireInterloper());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyVampire(Player player) {
        Permanent perm = new Permanent(new VampireInterloper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingNonVampire(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
