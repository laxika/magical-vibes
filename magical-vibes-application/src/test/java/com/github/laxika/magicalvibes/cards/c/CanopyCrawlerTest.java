package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({CanopyCrawler.class, Forest.class, WhiteKnight.class})
class CanopyCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Beast card revealed from your hand")
    void entersWithCounterForEachBeastCard() {
        CanopyCrawler card = new CanopyCrawler();
        harness.setHand(player1, List.of(card, new CanopyCrawler(), new CanopyCrawler(), new WhiteKnight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Canopy Crawler").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Beast cards in its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        CanopyCrawler card = new CanopyCrawler();
        harness.setHand(player1, List.of(card, new WhiteKnight()));
        harness.setHand(player2, List.of(new CanopyCrawler(), new CanopyCrawler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Canopy Crawler").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("May reveal only some of the Beast cards for amplify")
    void choosesSubsetOfBeastCardsForAmplify() {
        CanopyCrawler card = new CanopyCrawler();
        CanopyCrawler firstBeast = new CanopyCrawler();
        CanopyCrawler secondBeast = new CanopyCrawler();
        harness.setHand(player1, List.of(card, firstBeast, secondBeast, new WhiteKnight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBeast.getId(), secondBeast.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBeast.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Canopy Crawler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives a target creature +1/+1 for each +1/+1 counter on itself")
    void boostsTargetByItsPlusOneCounters() {
        Permanent crawler = addCreatureReady(player1, new CanopyCrawler());
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent crawler = addCreatureReady(player1, new CanopyCrawler());
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new CanopyCrawler());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
