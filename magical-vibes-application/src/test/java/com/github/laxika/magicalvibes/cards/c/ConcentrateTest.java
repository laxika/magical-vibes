package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConcentrateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        harness.setHand(player1, new ArrayList<>(List.of(new Concentrate())));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
