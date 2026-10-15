package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderOfUnity.class, DalkovanPackbeasts.class, Plains.class})
class ThunderOfUnityTest extends BaseCardTest {

    @Test
    void chapterI_drawsTwoCardsAndYouLoseTwoLife() {
        harness.setLibrary(player1, List.of(new DalkovanPackbeasts(), new DalkovanPackbeasts()));
        harness.setHand(player1, List.of(new ThunderOfUnity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void chapterII_drainsForEachCreatureYouControlEnteringThisTurn() {
        addSagaWithLore(1);
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        triggerNextChapter();
        harness.setHand(player1, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void chapterIII_delayedTriggerSurvivesSagaBeingSacrificed() {
        addSagaWithLore(2);
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        triggerNextChapter();
        harness.assertNotOnBattlefield(player1, "Thunder of Unity");

        harness.setHand(player1, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void chapterII_triggersForEveryEntryRatherThanOnlyTheFirst() {
        addSagaWithLore(1);
        triggerNextChapter();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void chapterII_doesNotTriggerForOpponentsCreaturesOrNoncreatures() {
        addSagaWithLore(1);
        triggerNextChapter();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new Plains());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void creatureEnteringBeforeChapterResolvesDoesNotTriggerRetroactively() {
        addSagaWithLore(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        resolveAllTriggers();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void chapterIII_triggerExpiresAtTheEndOfTheTurn() {
        addSagaWithLore(2);
        triggerNextChapter();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new DalkovanPackbeasts()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThunderOfUnity());
        saga.setCounterCount(CounterType.LORE, lore);
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
