package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwordOfBodyAndMindTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sword of Body and Mind has static +2/+2 boost effect")
    void hasStaticBoostEffect() {
        SwordOfBodyAndMind card = new SwordOfBodyAndMind();

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
    @DisplayName("Sword of Body and Mind has static protection from green and blue")
    void hasProtectionEffect() {
        SwordOfBodyAndMind card = new SwordOfBodyAndMind();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof ProtectionFromColorsEffect)
                .hasSize(1);
        ProtectionFromColorsEffect protection = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ProtectionFromColorsEffect)
                .map(e -> (ProtectionFromColorsEffect) e)
                .findFirst().orElseThrow();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("Sword of Body and Mind has combat damage token and mill effects")
    void hasCombatDamageEffects() {
        SwordOfBodyAndMind card = new SwordOfBodyAndMind();

        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects =
                card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
        assertThat(effects).hasSize(2);
        assertThat(effects).filteredOn(e -> e instanceof CreateTokenEffect).hasSize(1);
        assertThat(effects).filteredOn(e -> e instanceof MillTargetPlayerEffect).hasSize(1);

        MillTargetPlayerEffect mill = effects.stream()
                .filter(e -> e instanceof MillTargetPlayerEffect)
                .map(e -> (MillTargetPlayerEffect) e)
                .findFirst().orElseThrow();
        assertThat(mill.count()).isEqualTo(10);
    }

    @Test
    @DisplayName("Sword of Body and Mind has equip {2} ability")
    void hasEquipAbility() {
        SwordOfBodyAndMind card = new SwordOfBodyAndMind();

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

    // ===== Static effects: protection from green and blue =====

    @Test
    @DisplayName("Equipped creature has protection from green")
    void equippedCreatureHasProtectionFromGreen() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from blue")
    void equippedCreatureHasProtectionFromBlue() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
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

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    // ===== Combat damage trigger: token creation =====

    @Test
    @DisplayName("Creates a 2/2 green Wolf token when equipped creature deals combat damage to a player")
    void createsWolfTokenOnCombatDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> wolves = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(1);
        assertThat(wolves.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolves.getFirst().getCard().getSubtypes()).contains(CardSubtype.WOLF);
    }

    // ===== Combat damage trigger: mill =====

    @Test
    @DisplayName("Damaged player mills 10 cards when equipped creature deals combat damage")
    void millsTenCardsOnCombatDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Both token and mill trigger when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        // Wolf token created
        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(1);

        // 10 cards milled
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);

        // Combat damage dealt (creature has 4 power: 2 base + 2 from sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Handles library with fewer than 10 cards gracefully")
    void partialLibraryMill() {
        Permanent creature = addReadyCreature(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 3);

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
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

        setDeck(player2, 15);

        resolveCombat();

        // No wolf token created
        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).isEmpty();

        // No cards milled — deck still has all 15 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(15);
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
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.GREEN)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature1, CardColor.BLUE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature2, CardColor.BLUE)).isTrue();
    }

    // ===== Animated equipment (creature itself deals combat damage) =====

    @Test
    @DisplayName("Animated Sword creates Wolf token when it deals combat damage as a creature")
    void animatedSwordCreatesTokenOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(1);
        assertThat(wolves.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolves.getFirst().getCard().getSubtypes()).contains(CardSubtype.WOLF);
    }

    @Test
    @DisplayName("Animated Sword mills 10 cards when it deals combat damage as a creature")
    void animatedSwordMillsOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Animated Sword fires both token and mill when dealing combat damage as a creature")
    void animatedSwordFiresBothEffects() {
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();

        // Wolf token created
        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(1);

        // 10 cards milled
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);

        // Combat damage dealt (animated 3/3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Helpers =====

    private Permanent addAnimatedSword(Player player) {
        Permanent perm = new Permanent(new SwordOfBodyAndMind());
        perm.setSummoningSick(false);
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(3);
        perm.setAnimatedToughness(3);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSwordReady(Player player) {
        Permanent perm = new Permanent(new SwordOfBodyAndMind());
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

    private void setDeck(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        gd.playerDecks.put(player.getId(), deck);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
