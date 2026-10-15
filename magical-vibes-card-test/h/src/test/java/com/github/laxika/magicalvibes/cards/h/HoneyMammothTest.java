package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BloodCurdle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({HoneyMammoth.class, BloodCurdle.class})
class HoneyMammothTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 4 life")
    void etbGainsFourLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new HoneyMammoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertOnBattlefield(player1, "Honey Mammoth");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entry trigger still gains life after Honey Mammoth is destroyed")
    void gainsLifeAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new HoneyMammoth()));
        harness.setHand(player2, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 10);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Honey Mammoth"));
        harness.assertInGraveyard(player1, "Honey Mammoth");
        harness.assertNotOnBattlefield(player1, "Honey Mammoth");
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }
}
