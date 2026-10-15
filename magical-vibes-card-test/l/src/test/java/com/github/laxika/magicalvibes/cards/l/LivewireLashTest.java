package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GraftedExoskeleton;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Shunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivewireLash.class, GrizzlyBears.class, GiantGrowth.class, Shock.class,
        Boomerang.class, Shunt.class, GraftedExoskeleton.class})
class LivewireLashTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Livewire Lash to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0 from Livewire Lash")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());

        int effectivePower = gqs.getEffectivePower(gd, creature);
        int effectiveToughness = gqs.getEffectiveToughness(gd, creature);

        // Grizzly Bears is 2/2, should become 4/2
        assertThat(effectivePower).isEqualTo(4);
        assertThat(effectiveToughness).isEqualTo(2);
    }

    @Test
    @DisplayName("Trigger fires when equipped creature is targeted by a spell, prompts for any target")
    void triggerFiresWhenEquippedCreatureTargetedBySpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Trigger should prompt player1 (creature controller) to choose any target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Trigger deals 2 damage to chosen player target")
    void triggerDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

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

    @Test
    @DisplayName("Trigger does NOT fire when Livewire Lash is not attached to the targeted creature")
    void triggerDoesNotFireWhenNotAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new LivewireLash()); // Not attached to any creature

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        // No trigger should fire - spell resolves normally without prompting
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trigger does NOT fire when spell targets a player instead of equipped creature")
    void triggerDoesNotFireWhenSpellTargetsPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Target player2 (not the equipped creature)
        harness.castInstant(player1, 0, player2.getId());

        // No trigger - player target, not creature target
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trigger fires when equipped creature is targeted by a damage spell like Shock")
    void triggerFiresFromDamageSpell() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Trigger should prompt player1 to choose any target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());

        // Player1 targets player2
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability first (top of stack), then Shock
        harness.passBothPriorities(); // Resolve trigger - 2 damage to player2
        harness.passBothPriorities(); // Resolve Shock, which kills the equipped 4/2 creature

        // Player2 took 2 damage from the trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggered ability is put on the stack as TRIGGERED_ABILITY type")
    void triggeredAbilityOnStack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
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
        assertThat(trigger.getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Grizzly Bears's triggered ability targets Bob.");
    }

    @Test
    @DisplayName("Trigger fires when a spell is redirected onto the equipped creature via Shunt")
    void triggerFiresWhenSpellRetargetedOntoEquippedCreature() {
        harness.setLife(player2, 20);

        // Player1 has a creature with Livewire Lash and another unequipped creature
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(equippedCreature.getId());

        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());

        // Player1 casts Boomerang targeting player2's creature
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, otherCreature.getId());

        // No Livewire Lash trigger yet - the spell targets otherCreature, not the equipped one
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Player1 passes priority, player2 casts Shunt to redirect Boomerang
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Shunt - prompts player2 to choose new target
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Player2 retargets Boomerang onto player1's equipped creature
        harness.clearMessages();
        harness.handlePermanentChosen(player2, equippedCreature.getId());

        // Livewire Lash trigger should now fire - prompts player1 to choose any target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(harness.getConn1().getSentMessages()).hasSize(3);
        assertThat(harness.getConn1().getSentMessages().get(0)).contains("\"type\":\"GAME_STATE\"");
        assertThat(harness.getConn1().getSentMessages().get(1)).contains("\"type\":\"INTERACTION_PROMPT\"");
        assertThat(harness.getConn1().getSentMessages().get(2)).contains("\"type\":\"GAME_STATE\"");
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(2);
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"INTERACTION_PROMPT\"")).isEmpty();

        // Player1 targets player2 with the 2 damage
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability (top of stack), then Boomerang
        harness.passBothPriorities(); // Resolve trigger - 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature controller chooses the damage target even when an opponent controls the Lash")
    void creatureControllerChoosesTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Re-equipping moves the power boost to the new creature")
    void reEquippingMovesBoost() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        lash.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage from the equipped creature uses infect granted by another Equipment")
    void triggerDamageUsesGrantedInfect() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        lash.setAttachedTo(creature.getId());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting the equipped creature with an equip ability does not trigger damage")
    void activatedAbilityDoesNotTriggerDamage() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new LivewireLash());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        lash.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }
}
