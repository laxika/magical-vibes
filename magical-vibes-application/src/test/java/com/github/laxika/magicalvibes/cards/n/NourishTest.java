package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class NourishTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life")
    void gainsSixLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Nourish()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }
}
