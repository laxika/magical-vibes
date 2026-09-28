package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(AbbotOfTheSacredMeeple.class)
class AbbotOfTheSacredMeepleTest extends BaseCardTest {

    @Test
    void canBeCastAsACreature() {
        harness.setHand(player1, List.of(new AbbotOfTheSacredMeeple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Abbot of the Sacred Meeple");
    }
}
