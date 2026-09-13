package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorGuard.class, SpinedWurm.class})
class HonorGuardTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Honor Guard puts it on the stack")
    void castingPutsItOnStack() {
        HonorGuard guard = new HonorGuard();
        harness.setHand(player1, List.of(guard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(guard);
    }

    @Test
    @DisplayName("Resolving Honor Guard puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        HonorGuard guard = new HonorGuard();
        harness.setHand(player1, List.of(guard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == guard);
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent guardPerm = addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(guardPerm.getCard());
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Honor Guard")
    void resolvingAbilityBoostsToughness() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent guard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(1);
        assertThat(guard.getEffectiveToughness()).isEqualTo(2);
        assertThat(guard.getToughnessModifier()).isEqualTo(1);
        assertThat(guard.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving ability does not boost another creature")
    void resolvingAbilityOnlyBoostsHonorGuard() {
        addHonorGuardReady(player1);
        Permanent wurm = addCreatureReady(player1, new SpinedWurm());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getEffectivePower()).isEqualTo(5);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        // Activate and resolve three times
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(1);
        assertThat(guard.getEffectiveToughness()).isEqualTo(4);
        assertThat(guard.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent guardPerm = addHonorGuardReady(player1);
        guardPerm.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(guardPerm.getCard());
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        HonorGuard card = new HonorGuard();
        harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Activate twice and resolve
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(guard.getToughnessModifier()).isEqualTo(0);
        assertThat(guard.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Honor Guard before resolution prevents the boost")
    void abilityDoesNotBoostAfterSourceIsRemoved() {
        Permanent removedGuard = addHonorGuardReady(player1);
        Permanent remainingGuard = addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Honor Guard before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(removedGuard);

        harness.passBothPriorities();

        assertThat(remainingGuard.getEffectivePower()).isEqualTo(1);
        assertThat(remainingGuard.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addHonorGuardReady(player1);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only nonwhite mana")
    void cannotActivateWithOnlyNonWhiteMana() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability on permanent with no ability")
    void cannotActivateOnPermanentWithNoAbility() {
        addCreatureReady(player1, new SpinedWurm());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("activates")).isTrue();
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("gets +0/+1")).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addHonorGuardReady(Player player) {
        return addCreatureReady(player, new HonorGuard());
    }
}

