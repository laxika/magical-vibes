package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(SoothingBalm.class)
class SoothingBalmTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 5 life")
    void targetPlayerGainsFiveLife() {
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new SoothingBalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new SoothingBalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
    }
}
