package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PithDriller;
import com.github.laxika.magicalvibes.cards.c.ChainedThroatseeker;
import com.github.laxika.magicalvibes.cards.b.BlightedAgent;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfWarAndPeace.class, PithDriller.class, ChainedThroatseeker.class,
        BlightedAgent.class, VaultSkirge.class})
class SwordOfWarAndPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Sword for two mana")
    void hasEquipAbility() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6); // 4 + 2
    }

    @Test
    @DisplayName("Equipped creature loses boost when Sword is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature has protection from red")
    void equippedCreatureHasProtectionFromRed() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has protection from white")
    void equippedCreatureHasProtectionFromWhite() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have protection from blue")
    void equippedCreatureNoProtectionFromBlue() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses protection when Sword is removed")
    void creatureLosesProtectionWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new PithDriller());
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

    @Test
    @DisplayName("Deals damage to damaged player equal to the number of cards in their hand")
    void dealsDamageEqualToOpponentHandSize() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Give opponent 5 cards in hand
        harness.setHand(player2, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller(),
                new PithDriller(), new PithDriller())));
        harness.setHand(player1, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Combat damage: 4 (2 base + 2 sword) + 5 (hand size damage) = 9
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Deals no extra damage when opponent's hand is empty")
    void noExtraDamageWhenOpponentHandEmpty() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Only combat damage: 4 (2 base + 2 sword)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Controller gains 1 life per card in their hand")
    void gainsLifeEqualToControllerHandSize() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Give controller 3 cards in hand
        harness.setHand(player1, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller())));
        harness.setHand(player2, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Controller gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Controller gains no life when their hand is empty")
    void noLifeGainWhenControllerHandEmpty() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both damage and life gain fire when equipped creature deals combat damage")
    void bothEffectsFireOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Opponent has 4 cards, controller has 2 cards
        harness.setHand(player2, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller(), new PithDriller())));
        harness.setHand(player1, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller())));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Combat damage: 4 (2 base + 2 sword) + 4 (hand size damage) = 8 total to opponent
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);

        // Controller gains 2 life (2 cards in hand)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Blocker with 5 toughness survives the 4 power creature
        Permanent blocker = addCreatureReady(player2, new ChainedThroatseeker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setHand(player2, new ArrayList<>(List.of(new PithDriller(), new PithDriller())));
        harness.setHand(player1, new ArrayList<>(List.of(new PithDriller())));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // No hand-size damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sword can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature1 = addCreatureReady(player1, new PithDriller());
        Permanent creature2 = addCreatureReady(player1, new PithDriller());

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

    @Test
    @DisplayName("Animated Sword does not trigger hand-size damage from its own combat damage")
    void animatedSwordDoesNotTriggerHandSizeDamage() {
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        // Give opponent 5 cards in hand
        harness.setHand(player2, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller(),
                new PithDriller(), new PithDriller())));
        harness.setHand(player1, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Only the animated Sword's three combat damage is dealt.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Animated Sword does not trigger life gain from its own combat damage")
    void animatedSwordDoesNotTriggerLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        harness.setHand(player1, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller())));
        harness.setHand(player2, new ArrayList<>());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // The Sword is not an equipped creature.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Animated Sword triggers neither effect from its own combat damage")
    void animatedSwordDoesNotTriggerEitherEffect() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sword = addAnimatedSword(player1);
        sword.setAttacking(true);

        // Opponent has 4 cards, controller has 2 cards
        harness.setHand(player2, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller(), new PithDriller(), new PithDriller())));
        harness.setHand(player1, new ArrayList<>(List.of(
                new PithDriller(), new PithDriller())));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Only the animated Sword's three combat damage is dealt.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // No life gain triggers.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sword damage does not inherit the equipped creature's infect")
    void swordDamageDoesNotHaveInfect() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new PithDriller(), new PithDriller()));
        Permanent creature = addCreatureReady(player1, new BlightedAgent());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sword damage does not inherit the equipped creature's lifelink")
    void swordDamageDoesNotHaveLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new PithDriller(), new PithDriller()));
        Permanent creature = addCreatureReady(player1, new VaultSkirge());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Hand sizes are counted on resolution even after the Sword leaves")
    void usesCurrentHandsAfterSwordLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PithDriller()));
        harness.setHand(player2, List.of(new PithDriller()));
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.setHand(player1, List.of(new PithDriller(), new PithDriller(), new PithDriller()));
        harness.setHand(player2, List.of(new PithDriller(), new PithDriller()));
        gd.playerBattlefields.get(player1.getId()).remove(sword);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sword controller gains life when another player controls the equipped creature")
    void equipmentControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PithDriller(), new PithDriller()));
        harness.setHand(player2, List.of(new PithDriller()));
        Permanent creature = addCreatureReady(player1, new PithDriller());
        Permanent sword = addSwordReady(player2);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addAnimatedSword(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SwordOfWarAndPeace());
        perm.setSummoningSick(false);
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(3);
        perm.setAnimatedToughness(3);
        return perm;
    }

    private Permanent addSwordReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SwordOfWarAndPeace());
        perm.setSummoningSick(false);
        return perm;
    }
}
