package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GlimpseTheUnthinkable.class)
class GlimpseTheUnthinkableTest extends BaseCardTest {

    @Test
    @DisplayName("Mills ten cards from target player's library")
    void millsTenCards() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than ten")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 3) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
