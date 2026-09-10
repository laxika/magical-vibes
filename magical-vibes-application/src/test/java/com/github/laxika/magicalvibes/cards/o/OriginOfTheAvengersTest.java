package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BraveBrawler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfTheAvengers.class, BraveBrawler.class, LukeCagePowerMan.class, Forest.class,
        GrizzlyBears.class})
class OriginOfTheAvengersTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I scries two")
    void chapterIScriesTwo() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        addSaga(0);

        triggerChapter();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Chapter II puts only a Hero creature with mana value three or less onto the battlefield")
    void chapterIIPutsEligibleHeroCreature() {
        Card tooExpensiveHero = new LukeCagePowerMan();
        Card nonHero = new Forest();
        Card eligibleHero = new BraveBrawler();
        harness.setHand(player1, List.of(tooExpensiveHero, nonHero, eligibleHero));
        addSaga(1);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(2);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligibleHero);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensiveHero, nonHero);
    }

    @Test
    @DisplayName("Chapter II draws when no eligible Hero creature is in hand")
    void chapterIIDrawsWhenNoEligibleHeroCreature() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSaga(1);

        triggerChapter();
        int handSizeBeforeChoice = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeChoice + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Chapter II draws when the player declines")
    void chapterIIDrawsWhenDeclined() {
        Card hero = new BraveBrawler();
        harness.setHand(player1, List.of(hero));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSaga(1);

        triggerChapter();
        int handSizeBeforeChoice = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeChoice + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == hero);
    }

    @Test
    @DisplayName("Chapter III puts a +1/+1 counter on each creature you control")
    void chapterIIIPutsCountersOnControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(2);

        triggerChapter();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfTheAvengers());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
