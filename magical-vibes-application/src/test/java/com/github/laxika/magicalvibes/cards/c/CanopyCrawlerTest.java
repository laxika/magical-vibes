package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanopyCrawler.class, Forest.class, GrizzlyBears.class})
class CanopyCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Beast card revealed from your hand")
    void entersWithCounterForEachBeastCard() {
        CanopyCrawler card = new CanopyCrawler();
        harness.setHand(player1, List.of(card, new CanopyCrawler(), new CanopyCrawler(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Beast cards in its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        CanopyCrawler card = new CanopyCrawler();
        harness.setHand(player1, List.of(card, new GrizzlyBears()));
        harness.setHand(player2, List.of(new CanopyCrawler(), new CanopyCrawler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Gives a target creature +1/+1 for each +1/+1 counter on itself")
    void boostsTargetByItsPlusOneCounters() {
        Permanent crawler = addReadyCrawler();
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent crawler = addReadyCrawler();
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyCrawler();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCrawler() {
        Permanent crawler = new Permanent(new CanopyCrawler());
        crawler.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crawler);
        return crawler;
    }

    private Permanent findPermanentForCard(CanopyCrawler card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
