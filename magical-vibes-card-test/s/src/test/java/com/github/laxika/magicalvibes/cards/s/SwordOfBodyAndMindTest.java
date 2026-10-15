package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlintHawk;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfBodyAndMind.class, GlintHawk.class})
class SwordOfBodyAndMindTest extends BaseCardTest {

    @Test
    @DisplayName("Sword of Body and Mind has equip {2} ability")
    void hasEquipAbility() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Sword is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has protection from green")
    void equippedCreatureHasProtectionFromGreen() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from blue")
    void equippedCreatureHasProtectionFromBlue() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have protection from red")
    void equippedCreatureNoProtectionFromRed() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Creature loses protection when Sword is removed")
    void creatureLosesProtectionWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Creates a 2/2 green Wolf token when equipped creature deals combat damage to a player")
    void createsWolfTokenOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);
        assertThat(wolves.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(wolves.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolves.getFirst().getCard().getSubtypes()).contains(CardSubtype.WOLF);
    }

    @Test
    @DisplayName("Damaged player mills 10 cards when equipped creature deals combat damage")
    void millsTenCardsOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Both token and mill trigger when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        // Wolf token created
        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);

        // 10 cards milled
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);

        // Combat damage dealt (creature has 4 power: 2 base + 2 from sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Handles library with fewer than 10 cards gracefully")
    void partialLibraryMill() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        setDeck(player2, 3);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GlintHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        // No wolf token created
        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).isEmpty();

        // No cards milled — deck still has all 15 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(15);
    }

    @Test
    @DisplayName("Sword can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GlintHawk());
        Permanent creature2 = addCreatureReady(player1, new GlintHawk());

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

    @Test
    @DisplayName("Animated unattached Sword does not create a Wolf from its own combat damage")
    void animatedSwordDoesNotCreateTokenOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).isEmpty();
    }

    @Test
    @DisplayName("Animated unattached Sword does not mill from its own combat damage")
    void animatedSwordDoesNotMillOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(15);
    }

    @Test
    @DisplayName("Animated unattached Sword deals damage without triggering its equipment ability")
    void animatedSwordOnlyDealsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        setDeck(player2, 15);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).isEmpty();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Combat damage dealt (animated 3/3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Sword controller creates the Wolf when another player controls the equipped creature")
    void swordControllerGetsTokenFromOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        setDeck(player1, 15);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(15);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("An empty library does not prevent Wolf creation")
    void emptyLibraryStillCreatesWolf() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        setDeck(player2, 0);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private Permanent addAnimatedSword(Player player) {
        Permanent perm = addSwordReady(player);
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(3);
        perm.setAnimatedToughness(3);
        return perm;
    }

    private Permanent addSwordReady(Player player) {
        return addCreatureReady(player, new SwordOfBodyAndMind());
    }

    private void setDeck(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GlintHawk());
        }
        harness.setLibrary(player, deck);
    }
}
