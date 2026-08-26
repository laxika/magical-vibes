package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CityPigeon.class, Shock.class})
class CityPigeonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it leaves the battlefield")
    void createsFoodWhenLeavingBattlefield() {
        harness.addToBattlefield(player1, new CityPigeon());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "City Pigeon"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "City Pigeon");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("The created Food token can be sacrificed for life")
    void foodTokenCanBeSacrificed() {
        harness.addToBattlefield(player1, new CityPigeon());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "City Pigeon"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }
}
