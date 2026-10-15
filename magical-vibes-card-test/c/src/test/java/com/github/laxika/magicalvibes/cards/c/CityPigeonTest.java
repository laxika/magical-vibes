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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "City Pigeon"));
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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "City Pigeon"));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Returning City Pigeon to hand creates Food only when its trigger resolves")
    void createsFoodWhenReturnedToHand() {
        var pigeon = harness.addToBattlefieldAndReturn(player1, new CityPigeon());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, pigeon));

        harness.assertInHand(player1, "City Pigeon");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Food");
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Exiling an opponent's City Pigeon gives that opponent the Food")
    void createsFoodForOpponentWhenExiled() {
        var pigeon = harness.addToBattlefieldAndReturn(player2, new CityPigeon());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, pigeon));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "City Pigeon");
        harness.assertNotInGraveyard(player2, "City Pigeon");
        harness.assertOnBattlefield(player2, "Food");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Food is sacrificed as a cost before its life gain resolves")
    void foodSacrificePrecedesLifeGain() {
        var pigeon = harness.addToBattlefieldAndReturn(player1, new CityPigeon());
        harness.setLife(player1, 20);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, pigeon));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }
}
