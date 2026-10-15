package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnglowInfusion.class})
class DawnglowInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Only {G} spent: gain X life once")
    void greenOnlyGainsXOnce() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        // X=3 generic + {G/W} hybrid, all paid with green → only {G} spent.
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Only {W} spent: gain X life once")
    void whiteOnlyGainsXOnce() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("{G}{W} spent: gain X life twice")
    void bothColorsGainXTwice() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        // X=3 + {G/W}: give 2 of each so neither color alone covers the cost → both spent.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("X=0 gains no life")
    void zeroXGainsNoLife() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Dawnglow Infusion");
    }

    @Test
    @DisplayName("Green and red payment gains X life only once")
    void unrelatedColorDoesNotEnableWhiteLifeGain() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White and blue payment gains X life only once")
    void unrelatedColorDoesNotEnableGreenLifeGain() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertLife(player1, 23);
    }
}
