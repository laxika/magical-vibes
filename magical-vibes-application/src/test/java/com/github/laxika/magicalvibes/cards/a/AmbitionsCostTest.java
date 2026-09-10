package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AmbitionsCost.class)
class AmbitionsCostTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new AmbitionsCost(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Resolving loses three life")
    void losesThreeLife() {
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }
}
