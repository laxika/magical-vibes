package com.github.laxika.magicalvibes.cards.n;

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

class NightwingShadeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Nightwing Shade has correct activated ability")
    void hasCorrectAbility() {
        NightwingShade card = new NightwingShade();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{B}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Nightwing Shade puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new NightwingShade()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nightwing Shade");
    }

    @Test
    @DisplayName("Resolving Nightwing Shade puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new NightwingShade()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nightwing Shade"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent shadePerm = addNightwingShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Nightwing Shade");
        assertThat(entry.getTargetId()).isEqualTo(shadePerm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Nightwing Shade")
    void resolvingAbilityBoostsPowerAndToughness() {
        addNightwingShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addNightwingShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(5);
        assertThat(shade.getEffectiveToughness()).isEqualTo(5);
        assertThat(shade.getPowerModifier()).isEqualTo(3);
        assertThat(shade.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addNightwingShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addNightwingShadeReady(player1);
        // Only 1 black mana — needs {1}{B} (2 total with 1 black)
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helper methods =====

    private Permanent addNightwingShadeReady(Player player) {
        NightwingShade card = new NightwingShade();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
