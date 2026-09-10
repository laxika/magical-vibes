package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DownInTheValley.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class DownInTheValleyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I searches a basic land into its controller's hand")
    void chapterISearchesBasicLand() {
        addSagaWithLore(0);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, forest));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Chapter II grants landfall that creates a 1/1 green Elf")
    void chapterIICreatesElfOnLandfall() {
        addSagaWithLore(1);
        advanceToNextChapter();

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf")).isEqualTo(1);
        Permanent elf = findPermanent(player1, "Elf");
        assertThat(elf.getEffectivePower()).isEqualTo(1);
        assertThat(elf.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III boosts Elves and grants vigilance until end of turn")
    void chapterIIIBoostsElves() {
        assertChapterBuff(2);
    }

    @Test
    @DisplayName("Chapter IV boosts Elves and grants vigilance until end of turn")
    void chapterIVBoostsElves() {
        assertChapterBuff(3);
    }

    private void assertChapterBuff(int loreCounters) {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addSagaWithLore(loreCounters);

        advanceToNextChapter();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new DownInTheValley());
        Permanent saga = findPermanent(player1, "Down in the Valley");
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
