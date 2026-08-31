package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(ForceOfSavagery.class)
class ForceOfSavageryTest extends BaseCardTest {

    @Test
    @DisplayName("Dies immediately after entering because its toughness is 0")
    void diesImmediatelyAfterEntering() {
        harness.setHand(player1, List.of(new ForceOfSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Force of Savagery");
        harness.assertInGraveyard(player1, "Force of Savagery");
    }
}
