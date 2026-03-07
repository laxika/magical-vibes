package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Shunt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LivewireLashTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has static +2/+0 boost for equipped creature")
    void hasStaticBoost() {
        LivewireLash card = new LivewireLash();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect bace
                        && bace.powerBoost() == 2 && bace.toughnessBoost() == 0)
                .hasSize(1);
    }

    @Test
    @DisplayName("Has ON_BECOMES_TARGET_OF_SPELL effect dealing 2 damage to any target")
    void hasBecomesTargetTrigger() {
        LivewireLash card = new LivewireLash();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect =
                (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Livewire Lash to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lash = addLashReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+0 from Livewire Lash")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        int effectivePower = gqs.getEffectivePower(gd, creature);
        int effectiveToughness = gqs.getEffectiveToughness(gd, creature);

        // Grizzly Bears is 2/2, should become 4/2
        assertThat(effectivePower).isEqualTo(4);
        assertThat(effectiveToughness).isEqualTo(2);
    }

    // ===== Trigger fires when equipped creature is targeted by a spell =====

    @Test
    @DisplayName("Trigger fires when equipped creature is targeted by a spell, prompts for any target")
    void triggerFiresWhenEquippedCreatureTargetedBySpell() {
        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Trigger should prompt player1 (creature controller) to choose any target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Trigger deals 2 damage to chosen player target")
    void triggerDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Player1 chooses player2 as the target for the 2 damage trigger
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability goes on the stack, pass priority to resolve it
        // Stack now has: Giant Growth (bottom) + triggered ability (top)
        harness.passBothPriorities(); // Resolve triggered ability

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trigger deals 2 damage to chosen creature target")
    void triggerDeals2DamageToCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        Permanent targetCreature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Player1 targets opponent's creature with the 2 damage trigger
        harness.handlePermanentChosen(player1, targetCreature.getId());

        // Resolve triggered ability
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetCreature.getId()));
    }

    // ===== Trigger does NOT fire when equipment is not attached =====

    @Test
    @DisplayName("Trigger does NOT fire when Livewire Lash is not attached to the targeted creature")
    void triggerDoesNotFireWhenNotAttached() {
        Permanent creature = addReadyCreature(player1);
        addLashReady(player1); // Not attached to any creature

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // No trigger should fire - spell resolves normally without prompting
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Trigger does NOT fire when spell targets a player =====

    @Test
    @DisplayName("Trigger does NOT fire when spell targets a player instead of equipped creature")
    void triggerDoesNotFireWhenSpellTargetsPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Target player2 (not the equipped creature)
        harness.castInstant(player1, 0, player2.getId());

        // No trigger - player target, not creature target
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Trigger fires when equipped creature is targeted by damage spell =====

    @Test
    @DisplayName("Trigger fires when equipped creature is targeted by a damage spell like Shock")
    void triggerFiresFromDamageSpell() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Trigger should prompt player1 to choose any target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());

        // Player1 targets player2
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability first (top of stack), then Shock
        harness.passBothPriorities(); // Resolve trigger - 2 damage to player2
        harness.passBothPriorities(); // Resolve Shock - 2 damage to creature (4/2 with lash, survives)

        // Player2 took 2 damage from the trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Triggered ability is on the stack as TRIGGERED_ABILITY =====

    @Test
    @DisplayName("Triggered ability is put on the stack as TRIGGERED_ABILITY type")
    void triggeredAbilityOnStack() {
        Permanent creature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Player1 targets player2
        harness.handlePermanentChosen(player1, player2.getId());

        // Stack should have: Giant Growth (bottom) + triggered ability (top)
        assertThat(gd.stack).hasSize(2);
        StackEntry trigger = gd.stack.getLast();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== Trigger fires when spell is retargeted onto equipped creature (Shunt) =====

    @Test
    @DisplayName("Trigger fires when a spell is redirected onto the equipped creature via Shunt")
    void triggerFiresWhenSpellRetargetedOntoEquippedCreature() {
        harness.setLife(player2, 20);

        // Player1 has a creature with Livewire Lash and another unequipped creature
        Permanent equippedCreature = addReadyCreature(player1);
        Permanent lash = addLashReady(player1);
        lash.setAttachedTo(equippedCreature.getId());

        Permanent otherCreature = addReadyCreature(player2);

        // Player1 casts Boomerang targeting player2's creature
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, otherCreature.getId());

        // No Livewire Lash trigger yet - the spell targets otherCreature, not the equipped one
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Player1 passes priority, player2 casts Shunt to redirect Boomerang
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Shunt - prompts player2 to choose new target
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Player2 retargets Boomerang onto player1's equipped creature
        harness.handlePermanentChosen(player2, equippedCreature.getId());

        // Livewire Lash trigger should now fire - prompts player1 to choose any target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());

        // Player1 targets player2 with the 2 damage
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability (top of stack), then Boomerang
        harness.passBothPriorities(); // Resolve trigger - 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private Permanent addLashReady(Player player) {
        Permanent perm = new Permanent(new LivewireLash());
        perm.setSummoningSick(false);
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
