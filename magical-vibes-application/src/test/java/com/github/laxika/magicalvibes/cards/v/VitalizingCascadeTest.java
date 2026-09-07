package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VitalizingCascade.class)
class VitalizingCascadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains X plus 3 life")
    void gainsXPlusThree() {
        harness.setHand(player1, List.of(new VitalizingCascade()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(27);
    }

    @Test
    @DisplayName("X = 0 still gains 3 life")
    void gainsThreeWithZeroX() {
        harness.setHand(player1, List.of(new VitalizingCascade()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
