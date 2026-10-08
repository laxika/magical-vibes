package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChromeProwler;
import com.github.laxika.magicalvibes.cards.t.TekuthalInquiryDominus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VivisurgeonsInsight.class, ChromeProwler.class, TekuthalInquiryDominus.class})
class VivisurgeonsInsightTest extends BaseCardTest {

    @Test
    void drawsThreeCardsBeforeProliferating() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ChromeProwler());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void drawsThreeCardsWhenThereAreNoPermanentsToProliferate() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    void mayChooseNoPermanentsToProliferate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void proliferatesEachKindOnSelectedPermanentsFromBothBattlefields() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ChromeProwler());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        own.setCounterCount(CounterType.OIL, 3);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        opponent.setCounterCount(CounterType.OIL, 1);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new ChromeProwler());
        unchosen.setCounterCount(CounterType.OIL, 2);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player1, new ChromeProwler());

        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(own.getId(), opponent.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(own.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(opponent.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(withoutCounters.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void canProliferatePlayersAndPermanentsInTheSameChoice() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ChromeProwler());
        permanent.setCounterCount(CounterType.OIL, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(permanent.getId(), player1.getId(), player2.getId()));

        assertThat(permanent.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void proliferatesEveryCounterKindOnASelectedPlayerAndLeavesOtherPlayersUnchanged() {
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerExperienceCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 5);

        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
    }

    @Test
    void canChooseAPlayerWithOnlyEnergyCounters() {
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void doubledProliferationCanChoosePlayersWithOnlyEnergyOrExperienceCountersTwice() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player2.getId(), 3);

        harness.setHand(player1, List.of(new VivisurgeonsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
