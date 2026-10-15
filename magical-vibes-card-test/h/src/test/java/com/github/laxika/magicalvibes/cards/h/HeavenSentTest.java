package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeavenSent.class, Forest.class})
class HeavenSentTest extends BaseCardTest {

    @Test
    void castingSagaInvestigatesOnEntry() {
        harness.castFromHand(player1, new HeavenSent(), "{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertOnBattlefield(player1, "Heaven Sent");
    }

    @Test
    void firstTwoChaptersInvestigate() {
        addSagaWithLore(0);

        advanceToNextChapter();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        advanceToNextChapter();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void chapterThreeDrawsSevenWhenDamageLeavesOpponentAtZeroLife() {
        addSagaWithLore(2);
        gd.playerLifeTotals.put(player2.getId(), 1);
        List<Forest> library = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void chapterThreeOtherwiseExilesSagaAndAllowsCastingItThisTurn() {
        Permanent saga = addSagaWithLore(2);
        gd.playerLifeTotals.put(player2.getId(), 2);

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
        assertThat(gd.findExiledCard(saga.getCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(saga.getCard().getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(saga.getCard().getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, saga.getCard().getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Heaven Sent")).hasSize(1);
    }

    @Test
    void chapterThreeDoesNotDrawAndCanLeaveSagaExiled() {
        Permanent saga = addSagaWithLore(2);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int controllerLifeBefore = gd.getLife(player1.getId());

        advanceToNextChapter();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, controllerLifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        harness.assertNotOnBattlefield(player1, "Heaven Sent");
        harness.assertNotInGraveyard(player1, "Heaven Sent");
        assertThat(gd.findExiledCard(saga.getCard().getId())).isNotNull();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HeavenSent());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
