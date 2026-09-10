package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PainfulTruths.class)
class PainfulTruthsTest extends BaseCardTest {

    @Test
    @DisplayName("One color of mana draws one card and causes one life loss")
    void oneColorDrawsOneAndLosesOneLife() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        castWithMana(ManaColor.BLACK, ManaColor.BLACK, ManaColor.BLACK);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Three colors of mana draw three cards and cause three life loss")
    void threeColorsDrawThreeAndLoseThreeLife() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        castWithMana(ManaColor.BLACK, ManaColor.BLUE, ManaColor.WHITE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        harness.assertLife(player1, 17);
    }

    private void castWithMana(ManaColor... colors) {
        harness.setHand(player1, List.of(new PainfulTruths()));
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
