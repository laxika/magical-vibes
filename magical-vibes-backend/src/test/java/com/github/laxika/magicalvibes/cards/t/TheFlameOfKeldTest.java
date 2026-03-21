package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheFlameOfKeldTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has DiscardOwnHandEffect")
    void chapterIHasCorrectEffect() {
        TheFlameOfKeld card = new TheFlameOfKeld();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(DiscardOwnHandEffect.class);
    }

    @Test
    @DisplayName("Chapter II has DrawCardEffect(2)")
    void chapterIIHasCorrectEffect() {
        TheFlameOfKeld card = new TheFlameOfKeld();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) effects.getFirst()).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III has BoostColorSourceDamageThisTurnEffect(RED, 2)")
    void chapterIIIHasCorrectEffect() {
        TheFlameOfKeld card = new TheFlameOfKeld();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(BoostColorSourceDamageThisTurnEffect.class);
        BoostColorSourceDamageThisTurnEffect effect = (BoostColorSourceDamageThisTurnEffect) effects.getFirst();
        assertThat(effect.color()).isEqualTo(CardColor.RED);
        assertThat(effect.bonus()).isEqualTo(2);
    }

    // ===== Chapter I: discard your hand =====

    @Test
    @DisplayName("Chapter I discards controller's entire hand")
    void chapterIDiscardsHand() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setHand(player1, List.of(card1, card2));

        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new TheFlameOfKeld());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        // Saga just entered — lore counter already 1 from ETB, chapter I on stack
        // But we're adding it directly, so we need to trigger chapter I manually.
        // Instead, let's cast it properly:

        // Reset: cast the saga
        harness.setHand(player1, List.of(new TheFlameOfKeld(), new GrizzlyBears(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Remove the saga we added to battlefield earlier
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        // Chapter I ability on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");

        // Hand should still have the 2 remaining cards (GrizzlyBears and LightningBolt)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.passBothPriorities(); // resolve chapter I (discard hand)

        gd = harness.getGameData();
        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Cards should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter I with empty hand does nothing")
    void chapterIWithEmptyHand() {
        harness.setHand(player1, List.of(new TheFlameOfKeld()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Hand is now empty (spell was cast from hand)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();
        // Hand is still empty, no errors
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Chapter II: draw two cards =====

    @Test
    @DisplayName("Chapter II draws two cards")
    void chapterIIDrawsTwoCards() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Clear the hand
        harness.setHand(player1, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();
        // Should have drawn 2 cards (note: draw step also draws 1, so hand = 1 from draw step + 2 from chapter II = 3)
        // Actually — draw step is skipped when we advance from DRAW step. Let's just check deck size.
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore - 2);
    }

    // ===== Chapter III: red source damage boost =====

    @Test
    @DisplayName("Chapter III sets red source damage bonus for the controller")
    void chapterIIISetsRedDamageBonus() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        assertThat(gd.colorSourceDamageBonusThisTurn.getOrDefault(player1.getId(), java.util.Map.of()).getOrDefault(CardColor.RED, 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III boosts red spell damage by 2")
    void chapterIIIBoostsRedSpellDamage() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Now cast a Lightning Bolt targeting player2 — should deal 3 + 2 = 5 damage
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Lightning Bolt

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5 = 15
    }

    @Test
    @DisplayName("Chapter III does not boost non-red source damage")
    void chapterIIIDoesNotBoostNonRedDamage() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a green creature
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        assertThat(gd.colorSourceDamageBonusThisTurn.getOrDefault(player1.getId(), java.util.Map.of()).getOrDefault(CardColor.RED, 0)).isEqualTo(2);

        // Green creature combat damage should NOT be boosted
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        int combatDamage = gqs.applyCombatDamageMultiplier(gd, 2, bears, null);
        assertThat(combatDamage).isEqualTo(2); // no bonus for green
    }

    @Test
    @DisplayName("Chapter III boost applies to red combat damage")
    void chapterIIIBoostsRedCombatDamage() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a red 2/2 creature
        Card redCreature = new Card();
        redCreature.setName("Red Warrior");
        redCreature.setType(CardType.CREATURE);
        redCreature.setColor(CardColor.RED);
        redCreature.setColors(List.of(CardColor.RED));
        redCreature.setPower(2);
        redCreature.setToughness(2);
        redCreature.setToken(true);
        harness.addToBattlefield(player1, redCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Verify the combat damage multiplier includes the bonus
        GameData gd = harness.getGameData();
        Permanent redPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Red Warrior"))
                .findFirst().orElse(null);
        assertThat(redPerm).isNotNull();

        // Combat damage: 2 (base) + 2 (bonus) = 4
        int combatDamage = gqs.applyCombatDamageMultiplier(gd, 2, redPerm, null);
        assertThat(combatDamage).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III boost does not apply to green combat damage")
    void chapterIIIDoesNotBoostGreenCombatDamage() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a green creature (Grizzly Bears)
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        // Combat damage: 2 (base), no bonus for green
        int combatDamage = gqs.applyCombatDamageMultiplier(gd, 2, bears, null);
        assertThat(combatDamage).isEqualTo(2);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheFlameOfKeld());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Flame of Keld"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("The Flame of Keld"));
        assertThat(sagaOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("The Flame of Keld"));
        assertThat(sagaInGraveyard).isTrue();
    }
}
