package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScrollOfOrigins.class)
class ScrollOfOriginsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with seven or more cards in hand")
    void drawsWithAtLeastSevenCardsInHand() {
        harness.addToBattlefield(player1, new ScrollOfOrigins());
        harness.setHand(player1, hand(7));
        harness.setLibrary(player1, List.of(new ScrollOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Does not draw with fewer than seven cards in hand")
    void doesNotDrawWithFewerThanSevenCardsInHand() {
        harness.addToBattlefield(player1, new ScrollOfOrigins());
        harness.setHand(player1, hand(6));
        harness.setLibrary(player1, List.of(new ScrollOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Pays two mana and taps the source to activate")
    void paysManaAndTapsSourceToActivate() {
        var scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfOrigins());
        harness.setHand(player1, hand(6));
        harness.setLibrary(player1, List.of(new ScrollOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(scroll.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Checks hand size when the ability resolves")
    void checksHandSizeOnResolution() {
        harness.addToBattlefield(player1, new ScrollOfOrigins());
        harness.setHand(player1, hand(7));
        harness.setLibrary(player1, List.of(new ScrollOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerHands.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    private List<Card> hand(int count) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, count).forEach(index -> cards.add(new ScrollOfOrigins()));
        return cards;
    }
}
