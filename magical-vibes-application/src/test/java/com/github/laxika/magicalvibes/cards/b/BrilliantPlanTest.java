package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrilliantPlan.class)
class BrilliantPlanTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new BrilliantPlan(), "{4}{U}");
        harness.passBothPriorities();

        // Spell left hand, then three cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }
}
