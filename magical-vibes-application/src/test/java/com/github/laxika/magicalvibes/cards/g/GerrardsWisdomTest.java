package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GerrardsWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each other card in hand")
    void gains2LifePerCardInHand() {
        // Hand holds Gerrard's Wisdom plus 3 other cards. The spell leaves the
        // hand to the stack while resolving, so it counts the remaining 3.
        harness.setHand(player1, List.of(
                new GerrardsWisdom(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 3 cards * 2 life = 6, 20 + 6 = 26
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Gains no life with an otherwise empty hand")
    void gainsNoLifeWithEmptyHand() {
        harness.setHand(player1, List.of(new GerrardsWisdom()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
