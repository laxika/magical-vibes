package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfSpiderMan.class, GrizzlyBears.class})
class OriginOfSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 2/1 green Spider token with reach")
    void chapterICreatesSpiderToken() {
        addAndResolveSaga();

        Permanent spider = findPermanent(player1, "Spider");
        assertThat(spider).isNotNull();
        assertThat(spider.getCard().getPower()).isEqualTo(2);
        assertThat(spider.getCard().getToughness()).isEqualTo(1);
        assertThat(spider.getGrantedSubtypes()).isEmpty();
        assertThat(spider.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
        assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
    }

    @Test
    @DisplayName("Chapter II puts a counter on a creature and makes it a legendary Spider Hero")
    void chapterIIMakesCreatureLegendarySpiderHero() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfSpiderMan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.SPIDER, CardSubtype.HERO);
        assertThat(bears.getPersistentGrantedSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.hasEffectiveSupertype(gd, bears, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("Chapter III gives a creature double strike until end of turn")
    void chapterIIIGrantsDoubleStrike() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfSpiderMan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Chapters II and III only target creatures you control")
    void chaptersOnlyTargetOwnCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfSpiderMan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentBears.getId());
    }

    @Test
    @DisplayName("Chapter II requires choosing a creature when a legal target exists")
    void chapterIICannotBeSkipped() {
        addAndResolveSaga();
        Permanent spider = findPermanent(player1, "Spider");

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(spider.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("Chapter III requires choosing a creature when a legal target exists")
    void chapterIIICannotBeSkipped() {
        addAndResolveSaga();
        Permanent saga = findPermanent(player1, "Origin of Spider-Man");
        Permanent spider = findPermanent(player1, "Spider");
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(spider.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("Chapter II retains existing creature types and lasts beyond cleanup")
    void chapterIIChangesArePermanentAndAdditive() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfSpiderMan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.SPIDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.HERO)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, bears, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("The final chapter sacrifices the Saga and double strike expires at cleanup")
    void finalChapterSacrificesSagaAndDoubleStrikeExpires() {
        addAndResolveSaga();
        Permanent saga = findPermanent(player1, "Origin of Spider-Man");
        Permanent spider = findPermanent(player1, "Spider");
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.assertOnBattlefield(player1, "Origin of Spider-Man");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Origin of Spider-Man");
        harness.assertInGraveyard(player1, "Origin of Spider-Man");
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spider, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Chapter III excludes opposing creatures and noncreature permanents")
    void chapterIIIOnlyTargetsOwnCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfSpiderMan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void addAndResolveSaga() {
        harness.castFromHand(player1, new OriginOfSpiderMan(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
