package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.addToBattlefield(player1, new OriginOfSpiderMan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Origin of Spider-Man");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
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
        harness.addToBattlefield(player1, new OriginOfSpiderMan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Origin of Spider-Man");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Chapters II and III only target creatures you control")
    void chaptersOnlyTargetOwnCreatures() {
        harness.addToBattlefield(player1, new OriginOfSpiderMan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Origin of Spider-Man");
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentBears.getId());
    }

    private void addAndResolveSaga() {
        harness.setHand(player1, List.of(new OriginOfSpiderMan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
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
