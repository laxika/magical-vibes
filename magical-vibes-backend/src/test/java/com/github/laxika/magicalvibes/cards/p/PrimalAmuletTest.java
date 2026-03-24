package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalAmuletTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has cost reduction static effect")
    void frontFaceHasCostReduction() {
        PrimalAmulet card = new PrimalAmulet();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceCastCostForMatchingSpellsEffect.class);
        var reduction = (ReduceCastCostForMatchingSpellsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(reduction.amount()).isEqualTo(1);
        assertThat(reduction.scope()).isEqualTo(CostModificationScope.SELF);
    }

    @Test
    @DisplayName("Front face has spell cast trigger for charge counters and optional transform")
    void frontFaceHasSpellCastTrigger() {
        PrimalAmulet card = new PrimalAmulet();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);
        var trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst())
                .isInstanceOf(PutCounterOnSelfThenTransformIfThresholdEffect.class);
        var counterEffect = (PutCounterOnSelfThenTransformIfThresholdEffect) trigger.resolvedEffects().getFirst();
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.CHARGE);
        assertThat(counterEffect.threshold()).isEqualTo(4);
        assertThat(counterEffect.optional()).isTrue();
    }

    @Test
    @DisplayName("Has back face configured as Primal Wellspring")
    void hasBackFace() {
        PrimalAmulet card = new PrimalAmulet();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("PrimalWellspring");
    }

    @Test
    @DisplayName("Back face has mana ability with instant/sorcery copy")
    void backFaceHasManaAbility() {
        PrimalAmulet card = new PrimalAmulet();
        PrimalWellspring backFace = (PrimalWellspring) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(1);
        var ability = backFace.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardAnyColorManaWithInstantSorceryCopyEffect.class);
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Instant spells cost {1} less with Primal Amulet on the battlefield")
    void instantsCostOneLess() {
        Permanent amulet = addAmuletReady(player1);

        // Lightning Bolt costs {R}, which is already 1 mana — reduction of 1 generic makes it free of generic cost
        // but it still requires {R} (cost reduction only affects generic)
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Should be able to cast with just {R}
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Sorcery spells cost {1} less with Primal Amulet on the battlefield")
    void sorceriesCostOneLess() {
        Permanent amulet = addAmuletReady(player1);

        // Divination costs {2}{U} — with reduction it costs {1}{U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Cost reduction does not apply to creature spells")
    void costReductionDoesNotApplyToCreatures() {
        Permanent amulet = addAmuletReady(player1);

        // Grizzly Bears costs {1}{G} — should not get reduction
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Only {G} + 0 generic — not enough for {1}{G}

        // Should fail because creature spells don't get the reduction
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> harness.castCreature(player1, 0));
    }

    // ===== Charge counter trigger =====

    @Test
    @DisplayName("Casting an instant puts a charge counter on Primal Amulet")
    void castingInstantPutsChargeCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger (on top of stack above the spell)
        harness.passBothPriorities();

        assertThat(amulet.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery puts a charge counter on Primal Amulet")
    void castingSorceryPutsChargeCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(amulet.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not put a charge counter on Primal Amulet")
    void castingCreatureDoesNotPutCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        // No trigger should fire for creature spells
        assertThat(amulet.getChargeCounters()).isEqualTo(0);
    }

    // ===== Transform at 4 counters (optional) =====

    @Test
    @DisplayName("At 4+ charge counters, player may transform — accepting transforms")
    void transformsWhenAccepted() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setChargeCounters(3); // One more needed

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger — adds 4th counter, puts may ability on stack
        harness.passBothPriorities();
        // Resolve the may ability — prompts the player
        harness.passBothPriorities();
        // Accept the may transform
        harness.handleMayAbilityChosen(player1, true);

        assertThat(amulet.isTransformed()).isTrue();
        assertThat(amulet.getCard().getName()).isEqualTo("Primal Wellspring");
        assertThat(amulet.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("At 4+ charge counters, player may transform — declining keeps counters")
    void doesNotTransformWhenDeclined() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setChargeCounters(3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger — adds 4th counter, puts may ability on stack
        harness.passBothPriorities();
        // Resolve the may ability — prompts the player
        harness.passBothPriorities();
        // Decline the may transform
        harness.handleMayAbilityChosen(player1, false);

        assertThat(amulet.isTransformed()).isFalse();
        assertThat(amulet.getChargeCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("No may prompt when below 4 charge counters")
    void noMayPromptBelowThreshold() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setChargeCounters(2); // Will be 3 after trigger, still below 4

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger — adds counter, no may prompt
        harness.passBothPriorities();

        assertThat(amulet.getChargeCounters()).isEqualTo(3);
        assertThat(amulet.isTransformed()).isFalse();
    }

    // ===== Back face: Primal Wellspring mana ability with spell copy =====

    @Test
    @DisplayName("Primal Wellspring registers a pending spell copy trigger on mana ability activation")
    void wellspringRegistersPendingCopy() {
        Permanent wellspring = addTransformedWellspring(player1);

        int idx = indexOf(player1, wellspring);
        harness.activateAbility(player1, idx, 0, null, null);
        // Choose a color
        harness.handleListChoice(player1, "RED");

        // Pending copy should be registered
        assertThat(gd.pendingNextInstantSorceryCopyCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an instant with pending copy creates a copy on the stack")
    void wellspringCopiesInstantSpell() {
        // Directly set up the pending copy (simulating wellspring activation)
        gd.pendingNextInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Stack should have: original Lightning Bolt + copy triggered ability
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy") && e.getDescription().contains("Primal Wellspring"));
    }

    @Test
    @DisplayName("Casting a sorcery with pending copy creates a copy on the stack")
    void wellspringCopiesSorcerySpell() {
        gd.pendingNextInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy") && e.getDescription().contains("Primal Wellspring"));
    }

    @Test
    @DisplayName("Pending copy is one-shot — only first instant/sorcery gets copied")
    void pendingCopyIsOneShot() {
        gd.pendingNextInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Cast first bolt — should create copy
        harness.castInstant(player1, 0, player2.getId());
        long copyCountAfterFirst = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Copy") && e.getDescription().contains("Primal Wellspring"))
                .count();
        assertThat(copyCountAfterFirst).isEqualTo(1);

        // Pending copy should be consumed
        assertThat(gd.pendingNextInstantSorceryCopyCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a creature does not consume the pending copy trigger")
    void creatureDoesNotConsumePendingCopy() {
        gd.pendingNextInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        // No copy trigger and pending count still intact
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Primal Wellspring"));
        assertThat(gd.pendingNextInstantSorceryCopyCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Pending copy is cleared when mana pools drain")
    void pendingCopyClearedOnManaDrain() {
        gd.pendingNextInstantSorceryCopyCount.put(player1.getId(), 1);

        // Advance step — this drains mana pools and should clear pending copies
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance step, draining mana

        assertThat(gd.pendingNextInstantSorceryCopyCount).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addAmuletReady(Player player) {
        PrimalAmulet card = new PrimalAmulet();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedWellspring(Player player) {
        PrimalAmulet card = new PrimalAmulet();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        // Transform to back face
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
