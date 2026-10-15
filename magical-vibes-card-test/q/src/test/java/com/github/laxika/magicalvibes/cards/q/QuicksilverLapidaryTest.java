package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksilverLapidary.class, MoxOpal.class})
class QuicksilverLapidaryTest extends BaseCardTest {

    @Test
    void conjuresMoxOpalIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new QuicksilverLapidary());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Mox Opal");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void conjuresIntoTriggerControllersHandWithThatPlayerAsOwner() {
        harness.enterBattlefieldAndReturn(player2, new QuicksilverLapidary());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).singleElement().satisfies(card -> {
            assertThat(card).isInstanceOf(MoxOpal.class);
            assertThat(card.getOwnerId()).isEqualTo(player2.getId());
            assertThat(card.isToken()).isFalse();
        });
    }

    @Test
    void separateEntriesConjureDistinctCardsWithoutDrawingFromLibrary() {
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        harness.enterBattlefieldAndReturn(player1, new QuicksilverLapidary());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new QuicksilverLapidary());
        resolveAllTriggers();

        var hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(2).allSatisfy(card -> {
            assertThat(card).isInstanceOf(MoxOpal.class);
            assertThat(card.isToken()).isFalse();
        });
        assertThat(hand.get(0)).isNotSameAs(hand.get(1));
        assertThat(hand.get(0).getId()).isNotEqualTo(hand.get(1).getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
    }
}
