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

class FeralRidgewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Feral Ridgewolf has correct activated ability")
    void hasCorrectAbility() {
        FeralRidgewolf card = new FeralRidgewolf();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(2);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting Feral Ridgewolf puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FeralRidgewolf()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Feral Ridgewolf");
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Feral Ridgewolf");
        assertThat(entry.getTargetPermanentId()).isEqualTo(wolf.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +2/+0 to Feral Ridgewolf")
    void resolvingAbilityBoostsPower() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(3);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(7);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(wolf.getPowerModifier()).isEqualTo(6);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(5);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
        assertThat(wolf.getEffectivePower()).isEqualTo(1);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyWolf(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyWolf(Player player) {
        FeralRidgewolf card = new FeralRidgewolf();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
