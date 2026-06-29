package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FathomFleetFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Fathom Fleet Firebrand has correct activated ability")
    void hasCorrectAbility() {
        FathomFleetFirebrand card = new FathomFleetFirebrand();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting Fathom Fleet Firebrand puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FathomFleetFirebrand()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fathom Fleet Firebrand");
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent firebrand = addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fathom Fleet Firebrand");
        assertThat(entry.getTargetId()).isEqualTo(firebrand.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Fathom Fleet Firebrand")
    void resolvingAbilityBoostsPower() {
        Permanent firebrand = addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(3);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
        assertThat(firebrand.getPowerModifier()).isEqualTo(1);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent firebrand = addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(5);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
        assertThat(firebrand.getPowerModifier()).isEqualTo(3);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent firebrand = addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(4);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firebrand.getPowerModifier()).isEqualTo(0);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
        assertThat(firebrand.getEffectivePower()).isEqualTo(2);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyFirebrand(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only 1 red mana (needs {1}{R})")
    void cannotActivateWithOnlyOneRedMana() {
        addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyFirebrand(Player player) {
        FathomFleetFirebrand card = new FathomFleetFirebrand();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
