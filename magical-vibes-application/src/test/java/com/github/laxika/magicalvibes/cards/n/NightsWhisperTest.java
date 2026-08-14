package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightsWhisperTest extends BaseCardTest {

    @Test
    void drawsTwoCardsAndLosesTwoLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new NightsWhisper()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Night's Whisper");
    }

    @Test
    void losesTwoLifeEvenWhenLibraryHasOnlyOneCard() {
        harness.setLife(player1, 20);
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 1) {
            deck.removeFirst();
        }

        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
