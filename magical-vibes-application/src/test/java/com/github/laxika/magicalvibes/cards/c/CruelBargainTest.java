package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CruelBargainTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new CruelBargain()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving draws four cards")
    void drawsFourCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        cast();

        // Spell left hand, then four cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
    }

    @Test
    @DisplayName("From even life total, lose exactly half")
    void losesHalfFromEvenLife() {
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("From odd life total, lose half rounded up")
    void losesHalfRoundedUpFromOddLife() {
        harness.setLife(player1, 21);

        cast();

        // Half of 21 is 10.5, rounded up to 11 lost -> 10 remaining.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }
}
