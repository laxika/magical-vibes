package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwordOfFeastAndFamineTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sword of Feast and Famine has static +2/+2 boost effect")
    void hasStaticBoostEffect() {
        SwordOfFeastAndFamine card = new SwordOfFeastAndFamine();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect)
                .hasSize(1);
        BoostAttachedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostAttachedCreatureEffect)
                .map(e -> (BoostAttachedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sword of Feast and Famine has static protection from black and green")
    void hasProtectionEffect() {
        SwordOfFeastAndFamine card = new SwordOfFeastAndFamine();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof ProtectionFromColorsEffect)
                .hasSize(1);
        ProtectionFromColorsEffect protection = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ProtectionFromColorsEffect)
                .map(e -> (ProtectionFromColorsEffect) e)
                .findFirst().orElseThrow();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Sword of Feast and Famine has combat damage discard and untap effects")
    void hasCombatDamageEffects() {
        SwordOfFeastAndFamine card = new SwordOfFeastAndFamine();

        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects =
                card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
        assertThat(effects).hasSize(2);
        assertThat(effects).filteredOn(e -> e instanceof TargetPlayerDiscardsEffect).hasSize(1);
        assertThat(effects).filteredOn(e -> e instanceof UntapAllControlledPermanentsEffect).hasSize(1);

        TargetPlayerDiscardsEffect discard = effects.stream()
                .filter(e -> e instanceof TargetPlayerDiscardsEffect)
                .map(e -> (TargetPlayerDiscardsEffect) e)
                .findFirst().orElseThrow();
        assertThat(discard.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sword of Feast and Famine has equip {2} ability")
    void hasEquipAbility() {
        SwordOfFeastAndFamine card = new SwordOfFeastAndFamine();

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

    // ===== Static effects: protection from black and green =====

    @Test
    @DisplayName("Equipped creature has protection from black")
    void equippedCreatureHasProtectionFromBlack() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from green")
    void equippedCreatureHasProtectionFromGreen() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have protection from red")
    void equippedCreatureNoProtectionFromRed() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Creature loses protection when Sword is removed")
    void creatureLosesProtectionWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
    }

    // ===== Combat damage trigger: discard =====

    @Test
    @DisplayName("Damaged player must discard a card when equipped creature deals combat damage")
    void discardOnCombatDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // Game pauses for discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Untap still fires even when opponent has no cards to discard")
    void untapStillFiresWhenNoDiscard() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Discard does nothing, no input needed
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // But lands still untap (per MTG ruling)
        assertThat(land.isTapped()).isFalse();
    }

    // ===== Combat damage trigger: untap lands =====

    @Test
    @DisplayName("Untaps all lands controller controls when equipped creature deals combat damage")
    void untapLandsOnCombatDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Add tapped lands for player1
        Permanent land1 = addTappedLand(player1);
        Permanent land2 = addTappedLand(player1);

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Both lands should be untapped
        assertThat(land1.isTapped()).isFalse();
        assertThat(land2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's lands")
    void doesNotUntapOpponentLands() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent opponentLand = addTappedLand(player2);

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-land permanents")
    void doesNotUntapNonLandPermanents() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Add a tapped creature (not a land)
        Permanent otherCreature = addReadyCreature(player1);
        otherCreature.tap();

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Non-land should remain tapped
        assertThat(otherCreature.isTapped()).isTrue();
    }

    // ===== Both effects fire =====

    @Test
    @DisplayName("Both discard and untap fire when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent land1 = addTappedLand(player1);
        Permanent land2 = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // Discard prompt appears
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        harness.handleCardChosen(player2, 0);

        // Discard happened
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Lands untapped
        assertThat(land1.isTapped()).isFalse();
        assertThat(land2.isTapped()).isFalse();

        // Combat damage dealt (creature has 4 power: 2 base + 2 from sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== No trigger when blocked =====

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
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

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // No discard prompt
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Land still tapped
        assertThat(land.isTapped()).isTrue();
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
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.BLACK)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.GREEN)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.GREEN)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addSwordReady(Player player) {
        Permanent perm = new Permanent(new SwordOfFeastAndFamine());
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

    private Permanent addTappedLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        perm.setSummoningSick(false);
        perm.tap();
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
