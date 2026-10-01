package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PhosphorescentFeast.class, Tarmogoyf.class, PactOfNegation.class})
class PhosphorescentFeastTest extends BaseCardTest {

    private void pay(Player player) {
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gains 2 life for each green mana symbol among the cards left in hand")
    void gainsTwoLifePerGreenSymbol() {
        pay(player1);
        // Feast at index 0 leaves hand on cast; the two Tarmogoyfs ({1}{G} each) stay behind.
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new Tarmogoyf(), new Tarmogoyf()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Counts multiple green mana symbols on one card")
    void countsMultipleGreenSymbolsOnOneCard() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new PhosphorescentFeast()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Cards with no green mana symbols contribute no life")
    void noGreenSymbolsGainsZero() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new PactOfNegation()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
    }
}
