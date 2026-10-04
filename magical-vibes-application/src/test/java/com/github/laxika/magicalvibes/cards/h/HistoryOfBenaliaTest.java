package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({HistoryOfBenalia.class, BalothGorger.class, KnightOfGrace.class})
class HistoryOfBenaliaTest extends BaseCardTest {

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
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

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

    @Test
    @DisplayName("Chapter II creates a second Knight token")
    void chapterIICreatesSecondKnightToken() {
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();

        long knightCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight") && p.getCard().isToken())
                .count();
        assertThat(knightCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III gives +2/+1 to Knights you control until end of turn")
    void chapterIIIBoostsKnights() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

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
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

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
        saga.setCounterCount(CounterType.LORE, 2);

        // Add a non-Knight creature
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Baloth Gorger"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Baloth Gorger should remain unboosted
        bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Baloth Gorger"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        // Chapter III on stack — saga should still be on battlefield
        harness.assertOnBattlefield(player1, "History of Benalia");

        harness.passBothPriorities(); // resolve chapter III

        // Saga should be sacrificed
        harness.assertNotOnBattlefield(player1, "History of Benalia");

        // Saga should be in graveyard
        harness.assertInGraveyard(player1, "History of Benalia");
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("History of Benalia"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIIBoostsOnlyCurrentOwnKnightsAndExpiresAtCleanup() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).getFirst();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new KnightOfGrace());
        Permanent ownKnight = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.addToBattlefield(player2, new KnightOfGrace());
        Permanent opposingKnight = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(ownKnight.getPowerModifier()).isEqualTo(2);
        assertThat(ownKnight.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingKnight.getPowerModifier()).isZero();
        assertThat(opposingKnight.getToughnessModifier()).isZero();
        harness.assertNotOnBattlefield(player1, "History of Benalia");

        harness.setHand(player1, List.of(new KnightOfGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent laterKnight = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(laterKnight.getPowerModifier()).isZero();
        assertThat(laterKnight.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(ownKnight.getPowerModifier()).isZero();
        assertThat(ownKnight.getToughnessModifier()).isZero();
    }

    @Test
    void opponentsMainPhaseDoesNotAdvanceSaga() {
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).getFirst();
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

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
