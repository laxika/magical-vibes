package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Hundroog.class, GrizzlyBears.class})
class HundroogTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Hundroog and draws a card")
    void cyclingDiscardsHundroogAndDraws() {
        harness.setHand(player1, List.of(new Hundroog()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hundroog");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
