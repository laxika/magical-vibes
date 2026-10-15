package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainersTorment.class, AnointedProcession.class, SafePassage.class})
class ChainersTormentTest extends BaseCardTest {

    @Test
    @CardUsed(ChatterfangSquirrelGeneral.class)
    void addedSquirrelDealsTheSameFixedDamageAsTheNightmare() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Nightmare Horror")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        harness.assertLife(player1, 0);
    }

    @Test
    @CardUsed(JinnieFayJetmirsSecond.class)
    void replacementChoiceResumesTokenDamageWithTheOriginalFixedAmount() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.assertLife(player1, 20);
        harness.handleListChoice(player1, "Cat");

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player1, "Nightmare Horror")).isEmpty();
        harness.assertLife(player1, 10);
    }

    @Test
    void tokensEnteringUnderAnotherPlayersControlStillDamageTheSagaController() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playersGatheringSpecimensThisTurn.add(player2.getId());
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Nightmare Horror")).hasSize(1);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

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
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

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

    @Test
    @DisplayName("At the beginning of precombat main, Saga gets a second lore counter and triggers chapter II")
    void precombatMainTriggersChapterII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 1);

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
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        // Chapter II ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        // Resolve chapter II
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore + 2);
    }

    @Test
    @DisplayName("Chapter III creates X/X token where X is half life rounded up and deals X damage to controller")
    void chapterIIICreatesTokenAndDealsDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);

        // Set player1's life to 20, so X = ceil(20/2) = 10
        harness.setLife(player1, 20);

        // Advance to precombat main to trigger chapter III
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);

        // Set life to 15, X = ceil(15/2) = 8
        harness.setLife(player1, 15);

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

    @Test
    @DisplayName("Saga is sacrificed after final chapter ability resolves")
    void sagaSacrificedAfterFinalChapter() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.setLife(player1, 20);

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

        harness.assertNotOnBattlefield(player1, "Chainer's Torment");
        harness.assertInGraveyard(player1, "Chainer's Torment");
    }

    @Test
    @DisplayName("Saga is not sacrificed while its chapter ability is still on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();

        // Saga has 3 lore counters (>= final chapter)
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        // Chapter III is on the stack
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield (not yet sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

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
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }
    @Test
    @DisplayName("Each token created by chapter III deals X damage")
    void doubledTokensEachDealDamage() {
        harness.addToBattlefield(player1, new AnointedProcession());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Nightmare Horror"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(10);
                    assertThat(p.getCard().getToughness()).isEqualTo(10);
                });
        harness.assertLife(player1, 0);
    }

    @Test
    @DisplayName("Chapter III's token damage can be prevented")
    void tokenDamageIsPreventedBySafePassage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SafePassage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nightmare Horror");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chapter III uses the life total when it resolves")
    void tokenSizeUsesLifeTotalAtResolution() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ChainersTorment());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setLife(player1, 13);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Nightmare Horror"))
                .singleElement().satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(7);
                    assertThat(p.getCard().getToughness()).isEqualTo(7);
                });
        harness.assertLife(player1, 6);
    }
}
