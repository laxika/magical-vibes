package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HonorGuardTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Honor Guard has correct card properties")
    void hasCorrectProperties() {
        HonorGuard card = new HonorGuard();

        assertThat(card.getName()).isEqualTo("Honor Guard");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(0);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{W}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Honor Guard puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new HonorGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Honor Guard");
    }

    @Test
    @DisplayName("Resolving Honor Guard puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new HonorGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Honor Guard"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent guardPerm = addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Honor Guard");
        assertThat(entry.getTargetPermanentId()).isEqualTo(guardPerm.getId());
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
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Honor Guard");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        HonorGuard card = new HonorGuard();
        Permanent guardPerm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(guardPerm);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Honor Guard");
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
    @DisplayName("Ability fizzles if Honor Guard is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Honor Guard before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
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
    @DisplayName("Cannot activate ability on permanent with no ability")
    void cannotActivateOnPermanentWithNoAbility() {
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bear = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Permanent bearPerm = new Permanent(bear);
        bearPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bearPerm);
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

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Honor Guard's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addHonorGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gets +0/+1"));
    }

    // ===== Helper methods =====

    private Permanent addHonorGuardReady(Player player) {
        HonorGuard card = new HonorGuard();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

