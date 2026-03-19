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

class FieryHellhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Fiery Hellhound has correct activated ability")
    void hasCorrectAbility() {
        FieryHellhound card = new FieryHellhound();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{R}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting Fiery Hellhound puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FieryHellhound()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fiery Hellhound");
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fiery Hellhound");
        assertThat(entry.getTargetId()).isEqualTo(hellhound.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Fiery Hellhound")
    void resolvingAbilityBoostsPower() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(3);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(hellhound.getPowerModifier()).isEqualTo(1);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(5);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(hellhound.getPowerModifier()).isEqualTo(3);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(4);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellhound.getPowerModifier()).isEqualTo(0);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
        assertThat(hellhound.getEffectivePower()).isEqualTo(2);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyFieryHellhound(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyFieryHellhound(Player player) {
        FieryHellhound card = new FieryHellhound();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
