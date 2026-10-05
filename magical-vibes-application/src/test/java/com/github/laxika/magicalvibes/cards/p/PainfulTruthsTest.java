package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainfulTruths.class})
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

    @Test
    @DisplayName("Two distinct colors draw two cards even when one color is spent twice")
    void twoColorsDrawTwoAndLoseTwoLife() {
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());
        castWithMana(ManaColor.BLACK, ManaColor.BLUE, ManaColor.BLUE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        harness.assertInGraveyard(player1, "Painful Truths");
    }

    @Test
    @DisplayName("Colorless mana spent on the generic cost does not increase converge")
    void colorlessManaDoesNotCountAsAColor() {
        castWithMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.COLORLESS);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Colors added to the mana pool after casting do not change converge")
    void manaAddedAfterCastingDoesNotChangeConverge() {
        harness.castFromHand(player1, new PainfulTruths(), "{B}{B}{B}");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    private void castWithMana(ManaColor... colors) {
        harness.setHand(player1, List.of(new PainfulTruths()));
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, List.of());
    }
}
