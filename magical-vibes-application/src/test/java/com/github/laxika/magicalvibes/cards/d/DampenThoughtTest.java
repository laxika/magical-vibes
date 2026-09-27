package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampenThought.class, ReachThroughMists.class})
class DampenThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from target player's library")
    void millsFourCards() {
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        trimDeck(10);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than four")
    void millsOnlyRemaining() {
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        trimDeck(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        ReachThroughMists arcaneSpell = new ReachThroughMists();
        DampenThought dampenThought = new DampenThought();
        harness.setHand(player1, List.of(arcaneSpell, dampenThought));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        trimDeck(10);
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).contains(dampenThought);
    }

    private void trimDeck(int size) {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > size) {
            deck.removeFirst();
        }
    }
}
