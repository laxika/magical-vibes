package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfFeastAndFamine.class, GrizzlyBears.class, SerraAngel.class, Forest.class})
class SwordOfFeastAndFamineTest extends BaseCardTest {

    @Test
    @DisplayName("Sword of Feast and Famine has equip {2} ability")
    void hasEquipAbility() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(sword.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Sword is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has protection from black")
    void equippedCreatureHasProtectionFromBlack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from green")
    void equippedCreatureHasProtectionFromGreen() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have protection from red")
    void equippedCreatureNoProtectionFromRed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Creature loses protection when Sword is removed")
    void creatureLosesProtectionWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Damaged player must discard a card when equipped creature deals combat damage")
    void discardOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // Game pauses for discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Untap still fires even when opponent has no cards to discard")
    void untapStillFiresWhenNoDiscard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Discard does nothing, no input needed
        assertThat(gd.interaction.activeInteraction()).isNull();
        // But lands still untap (per MTG ruling)
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps all lands controller controls when equipped creature deals combat damage")
    void untapLandsOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Add a tapped creature (not a land)
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.tap();

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Non-land should remain tapped
        assertThat(otherCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both discard and untap fire when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent land1 = addTappedLand(player1);
        Permanent land2 = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // Discard prompt appears
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(land1.isTapped()).isTrue();
        assertThat(land2.isTapped()).isTrue();
        harness.handleCardChosen(player2, 0);

        // Discard happened
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Lands untapped
        assertThat(land1.isTapped()).isFalse();
        assertThat(land2.isTapped()).isFalse();

        // Combat damage dealt (creature has 4 power: 2 base + 2 from sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Sword controller untaps their lands when another player's equipped creature hits them")
    void swordControllerUntapsLandsRatherThanCreatureController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player2);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent creatureControllersLand = addTappedLand(player1);
        Permanent swordControllersLand = addTappedLand(player2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Forest");
        assertThat(swordControllersLand.isTapped()).isFalse();
        assertThat(creatureControllersLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // A white blocker can block despite protection from black and green.
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // No discard prompt
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Land still tapped
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sword can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

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

    @Test
    @DisplayName("Unattached animated Sword does not trigger discard from its own combat damage")
    void animatedSwordDoesNotTriggerDiscardOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Unattached animated Sword does not untap lands from its own combat damage")
    void animatedSwordDoesNotTriggerUntapOnCombatDamage() {
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unattached animated Sword deals damage without triggering either effect")
    void animatedSwordDealsDamageWithoutTriggering() {
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        Permanent land = addTappedLand(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(land.isTapped()).isTrue();

        // Combat damage dealt (animated 3/3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addAnimatedSword(Player player) {
        Permanent perm = addSwordReady(player);
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(3);
        perm.setAnimatedToughness(3);
        return perm;
    }

    private Permanent addSwordReady(Player player) {
        return addCreatureReady(player, new SwordOfFeastAndFamine());
    }

    private Permanent addTappedLand(Player player) {
        Permanent perm = addCreatureReady(player, new Forest());
        perm.tap();
        return perm;
    }
}
