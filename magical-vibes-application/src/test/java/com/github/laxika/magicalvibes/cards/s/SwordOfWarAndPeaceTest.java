package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwordOfWarAndPeaceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sword of War and Peace has static +2/+2 boost effect")
    void hasStaticBoostEffect() {
        SwordOfWarAndPeace card = new SwordOfWarAndPeace();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sword of War and Peace has static protection from red and white")
    void hasProtectionEffect() {
        SwordOfWarAndPeace card = new SwordOfWarAndPeace();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof ProtectionFromColorsEffect)
                .hasSize(1);
        ProtectionFromColorsEffect protection = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ProtectionFromColorsEffect)
                .map(e -> (ProtectionFromColorsEffect) e)
                .findFirst().orElseThrow();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    @Test
    @DisplayName("Sword of War and Peace has combat damage hand-size damage and life gain effects")
    void hasCombatDamageEffects() {
        SwordOfWarAndPeace card = new SwordOfWarAndPeace();

        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects =
                card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
        assertThat(effects).hasSize(2);
        assertThat(effects).filteredOn(e -> e instanceof DealDamageToTargetPlayerByHandSizeEffect).hasSize(1);
        assertThat(effects).filteredOn(e -> e instanceof GainLifePerCardsInHandEffect).hasSize(1);
    }

    @Test
    @DisplayName("Sword of War and Peace has equip {2} ability")
    void hasEquipAbility() {
        SwordOfWarAndPeace card = new SwordOfWarAndPeace();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Sword is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== Static effects: protection from red and white =====

    @Test
    @DisplayName("Equipped creature has protection from red")
    void equippedCreatureHasProtectionFromRed() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from white")
    void equippedCreatureHasProtectionFromWhite() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have protection from blue")
    void equippedCreatureNoProtectionFromBlue() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses protection when Sword is removed")
    void creatureLosesProtectionWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Unequipped Sword itself does NOT have protection from red or white")
    void swordItselfHasNoProtection() {
        Permanent sword = addSwordReady(player1);

        assertThat(gqs.hasProtectionFrom(gd, sword, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, sword, CardColor.WHITE)).isFalse();
    }

    // ===== Combat damage trigger: damage to player by their hand size =====

    @Test
    @DisplayName("Deals damage to damaged player equal to the number of cards in their hand")
    void dealsDamageEqualToOpponentHandSize() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Give opponent 5 cards in hand
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();

        // Combat damage: 4 (2 base + 2 sword) + 5 (hand size damage) = 9
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Deals no extra damage when opponent's hand is empty")
    void noExtraDamageWhenOpponentHandEmpty() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();

        // Only combat damage: 4 (2 base + 2 sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Combat damage trigger: life gain by controller's hand size =====

    @Test
    @DisplayName("Controller gains 1 life per card in their hand")
    void gainsLifeEqualToControllerHandSize() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Give controller 3 cards in hand
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Controller gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Controller gains no life when their hand is empty")
    void noLifeGainWhenControllerHandEmpty() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Both effects fire =====

    @Test
    @DisplayName("Both damage and life gain fire when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Opponent has 4 cards, controller has 2 cards
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears())));

        resolveCombat();

        // Combat damage: 4 (2 base + 2 sword) + 4 (hand size damage) = 8 total to opponent
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);

        // Controller gains 2 life (2 cards in hand)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== No trigger when blocked =====

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Blocker with 5 toughness survives the 4 power creature
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // No hand-size damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Sword can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        sword.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.RED)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.WHITE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.WHITE)).isTrue();
    }

    // ===== Animated equipment (creature itself deals combat damage) =====

    @Test
    @DisplayName("Animated Sword deals damage by opponent hand size when it deals combat damage as a creature")
    void animatedSwordDealsDamageByHandSize() {
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        // Give opponent 5 cards in hand
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();

        // Combat damage: 3 (animated) + 5 (hand size damage) = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Animated Sword grants life by controller hand size when it deals combat damage as a creature")
    void animatedSwordGrantsLifeByHandSize() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Controller gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Animated Sword fires both damage and life gain when dealing combat damage as a creature")
    void animatedSwordFiresBothEffects() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        // Opponent has 4 cards, controller has 2 cards
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears())));

        resolveCombat();

        // Combat damage: 3 (animated) + 4 (hand size damage) = 7 total to opponent
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        // Controller gains 2 life (2 cards in hand)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== Helpers =====

    private Permanent addAnimatedSword(Player player) {
        Permanent perm = new Permanent(new SwordOfWarAndPeace());
        perm.setSummoningSick(false);
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(3);
        perm.setAnimatedToughness(3);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSwordReady(Player player) {
        Permanent perm = new Permanent(new SwordOfWarAndPeace());
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

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
