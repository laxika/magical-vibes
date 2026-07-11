package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrilliantPlanTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new BrilliantPlan()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Spell left hand, then three cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }
}
