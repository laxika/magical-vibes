package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryOfBenaliaTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I creates a 2/2 white Knight token with vigilance")
    void chapterIHasCorrectEffects() {
        HistoryOfBenalia card = new HistoryOfBenalia();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Knight");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.WHITE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(effect.keywords()).containsExactly(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Chapter II has same token creation as chapter I")
    void chapterIIHasCorrectEffects() {
        HistoryOfBenalia card = new HistoryOfBenalia();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Knight");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III boosts own Knights +2/+1")
    void chapterIIIHasCorrectEffects() {
        HistoryOfBenalia card = new HistoryOfBenalia();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        BoostAllOwnCreaturesEffect effect = (BoostAllOwnCreaturesEffect) effects.getFirst();
        assertThat(effect.powerBoost()).isEqualTo(2);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting History of Benalia adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I resolving creates a 2/2 white Knight token with vigilance")
    void chapterICreatesKnightToken() {
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(knight).isNotNull();
        assertThat(knight.getCard().getPower()).isEqualTo(2);
        assertThat(knight.getCard().getToughness()).isEqualTo(2);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    // ===== Precombat main: chapter II triggers =====

    @Test
    @DisplayName("Chapter II creates a second Knight token")
    void chapterIICreatesSecondKnightToken() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

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

        long knightCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight") && p.getCard().isToken())
                .count();
        assertThat(knightCount).isEqualTo(1);
    }

    // ===== Chapter III: Knights you control get +2/+1 =====

    @Test
    @DisplayName("Chapter III gives +2/+1 to Knights you control until end of turn")
    void chapterIIIBoostsKnights() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a Knight token to the battlefield to verify the boost
        harness.addToBattlefield(player1, createKnightToken());

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(knight).isNotNull();
        assertThat(knight.getCard().getPower()).isEqualTo(2);
        assertThat(knight.getCard().getToughness()).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(3);

        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // Knight should now be boosted to 4/3
        knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(knight).isNotNull();
        assertThat(knight.getCard().getPower() + knight.getPowerModifier()).isEqualTo(4);
        assertThat(knight.getCard().getToughness() + knight.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III does not boost non-Knight creatures")
    void chapterIIIDoesNotBoostNonKnights() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a non-Knight creature
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Grizzly Bears should remain unboosted
        bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Chapter III on stack — saga should still be on battlefield
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("History of Benalia"));
        assertThat(sagaOnBf).isTrue();

        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("History of Benalia"));
        assertThat(sagaStillOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("History of Benalia"));
        assertThat(sagaInGraveyard).isTrue();
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();

        assertThat(saga.getLoreCounters()).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    // ===== Helper =====

    private com.github.laxika.magicalvibes.model.Card createKnightToken() {
        var token = new com.github.laxika.magicalvibes.model.Card();
        token.setToken(true);
        token.setName("Knight");
        token.setPower(2);
        token.setToughness(2);
        token.setColor(CardColor.WHITE);
        token.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        token.setSubtypes(List.of(CardSubtype.KNIGHT));
        token.setKeywords(java.util.Set.of(Keyword.VIGILANCE));
        return token;
    }
}
