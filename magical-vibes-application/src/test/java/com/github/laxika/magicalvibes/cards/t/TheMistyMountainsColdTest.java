package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMistyMountainsCold.class})
class TheMistyMountainsColdTest extends BaseCardTest {

    @Test
    void chaptersCreateTreasuresAndChapterIVCreatesDragonAfterSacrifice() {
        Permanent saga = addSagaWithLore(0);

        resolveChapter(saga, 0);
        resolveChapter(saga, 1);
        resolveChapter(saga, 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);

        resolveChapter(saga, 3);

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(6);
        assertThat(dragon.getCard().getToughness()).isEqualTo(6);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIVDoesNotCreateDragonWithFewerThanFourTreasures() {
        Permanent saga = addSagaWithLore(0);

        resolveChapter(saga, 0);
        resolveChapter(saga, 1);
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void earlyChaptersSacrificeSagaAndCreateDragonWhenFourthTreasureIsCreated(int lore) {
        Permanent saga = addSagaWithLore(0);
        resolveChapter(saga, 0);
        resolveChapter(saga, 1);
        resolveChapter(saga, 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);

        resolveChapter(saga, lore);

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIVCreatesTreasureButNoDragonIfSagaLeavesBeforeResolution() {
        Permanent saga = addSagaWithLore(0);
        resolveChapter(saga, 0);
        resolveChapter(saga, 1);
        resolveChapter(saga, 2);
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerGraveyards.get(player1.getId()).add(saga.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void chapterIVDoesNotCountOpponentsTreasures() {
        Permanent saga = addSagaWithLore(0);
        resolveChapter(saga, 0);
        resolveChapter(saga, 1);
        resolveChapter(saga, 2);
        var treasures = findPermanents(player1, "Treasure");
        gd.playerBattlefields.get(player1.getId()).removeAll(treasures);
        gd.playerBattlefields.get(player2.getId()).addAll(treasures);

        resolveChapter(saga, 3);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).hasSize(3);
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMistyMountainsCold());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveChapter(Permanent saga, int lore) {
        saga.setCounterCount(CounterType.LORE, lore);
        advanceToNextChapter();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
