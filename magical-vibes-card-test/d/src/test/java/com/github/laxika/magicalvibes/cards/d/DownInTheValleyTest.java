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
    @DisplayName("Entering the battlefield triggers chapter I immediately")
    void enteringTriggersChapterI() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromHand(player1, new DownInTheValley(), "{2}{G}");
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(findPermanent(player1, "Down in the Valley").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I may fail to find even when a basic land is available")
    void chapterIMayFailToFind() {
        addSagaWithLore(0);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        advanceToNextChapter();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter I completes when the library contains no basic land")
    void chapterIWithNoBasicLand() {
        addSagaWithLore(0);
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
    @DisplayName("Landfall is not granted before chapter II resolves")
    void noLandfallBeforeChapterII() {
        addSagaWithLore(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf")).isZero();
    }

    @Test
    @DisplayName("Chapter II does not trigger for an opponent's land")
    void opponentLandDoesNotTriggerLandfall() {
        addSagaWithLore(1);
        advanceToNextChapter();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf")).isZero();
        assertThat(countPermanents(player2, "Elf")).isZero();
    }

    @Test
    @DisplayName("The landfall ability survives end-of-turn cleanup")
    void landfallPersistsAfterCleanup() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving chapter II again grants another landfall ability")
    void repeatedChapterIIGrantsAdditionalLandfall() {
        Permanent saga = addSagaWithLore(1);
        advanceToNextChapter();
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf")).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III affects only Elves controlled when it resolves")
    void chapterBuffExcludesOpponentsAndLaterElves() {
        Permanent ownElf = addCreatureReady(player1, new LlanowarElves());
        Permanent opposingElf = addCreatureReady(player2, new LlanowarElves());
        addSagaWithLore(2);
        advanceToNextChapter();
        Permanent laterElf = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingElf, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, laterElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The Saga is sacrificed after chapter IV while its buff remains")
    void chapterIVSacrificesSagaAfterResolution() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        addSagaWithLore(3);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Down in the Valley");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Down in the Valley");
        harness.assertInGraveyard(player1, "Down in the Valley");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isTrue();
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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DownInTheValley());
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
