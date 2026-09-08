package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MacetailHystrodon.class, GrizzlyBears.class})
class MacetailHystrodonTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Macetail Hystrodon and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Macetail Hystrodon");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
