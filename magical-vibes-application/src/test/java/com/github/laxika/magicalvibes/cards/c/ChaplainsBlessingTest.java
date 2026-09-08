package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ChaplainsBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 5 life")
    void gainsFiveLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ChaplainsBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }
}
