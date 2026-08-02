package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class JushiApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card without flipping when the hand stays below nine cards")
    void drawsWithoutFlipping() {
        Permanent apprentice = addApprentice();
        harness.setHand(player1, hand(3));
        harness.setLibrary(player1, hand(5));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(apprentice.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips when the draw brings the hand to nine cards")
    void flipsAtNineCards() {
        Permanent apprentice = addApprentice();
        harness.setHand(player1, hand(8));
        harness.setLibrary(player1, hand(5));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(apprentice.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Tomoya makes the target player draw cards equal to the controller's hand size")
    void tomoyaDrawsEqualToHandSize() {
        Permanent apprentice = addApprentice();
        apprentice.setTransformed(true);
        apprentice.setCard(apprentice.getOriginalCard().getBackFaceCard());
        harness.setHand(player1, hand(3));
        harness.setHand(player2, hand(0));
        harness.setLibrary(player2, hand(10));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private Permanent addApprentice() {
        Permanent apprentice = new Permanent(new JushiApprentice());
        apprentice.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(apprentice);
        return apprentice;
    }

    private List<Card> hand(int count) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, count).forEach(index -> cards.add(new GrizzlyBears()));
        return cards;
    }
}
