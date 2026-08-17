package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SoothingBalmTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 5 life")
    void targetPlayerGainsFiveLife() {
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new SoothingBalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new SoothingBalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
