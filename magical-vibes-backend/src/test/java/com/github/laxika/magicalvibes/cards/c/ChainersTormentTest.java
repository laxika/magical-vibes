package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChainersTormentTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has deal 2 damage to each opponent and gain 2 life effects")
    void chapterIHasCorrectEffects() {
        ChainersTorment card = new ChainersTorment();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(DealDamageToEachOpponentEffect.class);
        assertThat(((DealDamageToEachOpponentEffect) effects.get(0)).damage()).isEqualTo(2);
        assertThat(effects.get(1)).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) effects.get(1)).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter II has same effects as chapter I")
    void chapterIIHasCorrectEffects() {
        ChainersTorment card = new ChainersTorment();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(DealDamageToEachOpponentEffect.class);
        assertThat(effects.get(1)).isInstanceOf(GainLifeEffect.class);
    }

    @Test
    @DisplayName("Chapter III creates Nightmare Horror token from half life total")
    void chapterIIIHasCorrectEffects() {
        ChainersTorment card = new ChainersTorment();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.get(0)).isInstanceOf(CreateTokenFromHalfLifeTotalAndDealDamageEffect.class);
        CreateTokenFromHalfLifeTotalAndDealDamageEffect effect =
                (CreateTokenFromHalfLifeTotalAndDealDamageEffect) effects.get(0);
        assertThat(effect.tokenName()).isEqualTo("Nightmare Horror");
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.NIGHTMARE, CardSubtype.HORROR);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting Chainer's Torment adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new ChainersTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        // Resolve the enchantment spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Saga should be on the battlefield with 1 lore counter
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I resolving deals 2 damage to opponent and gains 2 life")
    void chapterIResolvesDamageAndLife() {
        harness.setHand(player1, List.of(new ChainersTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I ability

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore + 2);
    }

    // ===== Precombat main: lore counter addition =====

    @Test
    @DisplayName("At the beginning of precombat main, Saga gets a second lore counter and triggers chapter II")
    void precombatMainTriggersChapterII() {
        harness.addToBattlefield(player1, new ChainersTorment());
        // Manually set lore counter to 1 (simulating ETB already happened)
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Advance to precombat main on player1's turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        // Skip the draw step to get to precombat main
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Saga should now have 2 lore counters
        assertThat(saga.getLoreCounters()).isEqualTo(2);

        // Chapter II ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        // Resolve chapter II
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore + 2);
    }

    // ===== Chapter III: token creation and self-damage =====

    @Test
    @DisplayName("Chapter III creates X/X token where X is half life rounded up and deals X damage to controller")
    void chapterIIICreatesTokenAndDealsDamage() {
        harness.addToBattlefield(player1, new ChainersTorment());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Set player1's life to 20, so X = ceil(20/2) = 10
        gd.playerLifeTotals.put(player1.getId(), 20);

        // Advance to precombat main to trigger chapter III
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(3);

        // Resolve chapter III
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Token should be on battlefield: 10/10 Nightmare Horror
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nightmare Horror") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(10);
        assertThat(token.getCard().getToughness()).isEqualTo(10);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.NIGHTMARE, CardSubtype.HORROR);

        // Controller should have taken 10 damage: 20 - 10 = 10
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Chapter III with odd life total rounds X up")
    void chapterIIIRoundsUp() {
        harness.addToBattlefield(player1, new ChainersTorment());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Set life to 15, X = ceil(15/2) = 8
        gd.playerLifeTotals.put(player1.getId(), 15);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Token should be 8/8
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nightmare Horror") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(8);
        assertThat(token.getCard().getToughness()).isEqualTo(8);

        // 15 - 8 = 7
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
    }

    // ===== Saga sacrifice SBA =====

    @Test
    @DisplayName("Saga is sacrificed after final chapter ability resolves")
    void sagaSacrificedAfterFinalChapter() {
        harness.addToBattlefield(player1, new ChainersTorment());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        gd.playerLifeTotals.put(player1.getId(), 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Chapter III is on the stack — Saga should NOT be sacrificed yet
        Permanent sagaStillAlive = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(sagaStillAlive).isNotNull();

        // Resolve chapter III
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Saga should now be sacrificed (no longer on battlefield)
        boolean sagaOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Chainer's Torment"));
        assertThat(sagaOnBattlefield).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Chainer's Torment"));
        assertThat(sagaInGraveyard).isTrue();
    }

    // ===== Saga not sacrificed while chapter ability is on the stack =====

    @Test
    @DisplayName("Saga is not sacrificed while its chapter ability is still on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new ChainersTorment());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();

        // Saga has 3 lore counters (>= final chapter)
        assertThat(saga.getLoreCounters()).isEqualTo(3);
        // Chapter III is on the stack
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield (not yet sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    // ===== Saga doesn't get lore counter on the turn it enters =====

    @Test
    @DisplayName("Saga doesn't get an additional lore counter on the turn it's cast")
    void sagaNoExtraLoreCounterOnCastTurn() {
        harness.setHand(player1, List.of(new ChainersTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // We're in precombat main (default setup)
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I ability

        GameData gd = harness.getGameData();
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chainer's Torment"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        // Should still have exactly 1 lore counter (not 2)
        assertThat(saga.getLoreCounters()).isEqualTo(1);
    }
}
