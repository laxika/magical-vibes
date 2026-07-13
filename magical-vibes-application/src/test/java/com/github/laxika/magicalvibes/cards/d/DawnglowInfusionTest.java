package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnglowInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Only {G} spent: gain X life once")
    void greenOnlyGainsXOnce() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        // X=3 generic + {G/W} hybrid, all paid with green → only {G} spent.
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Only {W} spent: gain X life once")
    void whiteOnlyGainsXOnce() {
        harness.setHand(player1, List.of(new DawnglowInfusion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }
}
