package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LoomingShade.class)
class LoomingShadeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Looming Shade puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new LoomingShade(), "{2}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Looming Shade puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.castFromHand(player1, new LoomingShade(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Looming Shade");
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent shadePerm = addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(shadePerm.getId());
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Looming Shade")
    void resolvingAbilityBoostsPowerAndToughness() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);
        assertThat(shade.getPowerModifier()).isEqualTo(3);
        assertThat(shade.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent shadePerm = addCreatureReady(player1, new LoomingShade());
        shadePerm.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability fizzles if Looming Shade is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Looming Shade before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new LoomingShade());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only nonblack mana")
    void cannotActivateWithOnlyNonblackMana() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("activates Looming Shade's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addCreatureReady(player1, new LoomingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gets +1/+1"));
    }

}

