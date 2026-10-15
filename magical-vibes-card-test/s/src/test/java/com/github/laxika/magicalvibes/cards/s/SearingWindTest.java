package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({SearingWind.class, DivingGriffin.class, Boomerang.class})
class SearingWindTest extends BaseCardTest {

    @Test
    @DisplayName("Searing Wind deals 10 damage to target player")
    void deals10DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Searing Wind deals 10 damage to target creature, destroying a 2/2")
    void deals10DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        UUID targetId = harness.getPermanentId(player2, "Diving Griffin");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Diving Griffin");
        harness.assertInGraveyard(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("Searing Wind can deal 10 damage to its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Searing Wind");
    }

    @Test
    @DisplayName("Searing Wind can target its controller's creature without damaging either player")
    void canTargetItsControllersCreature() {
        harness.addToBattlefield(player1, new DivingGriffin());
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Diving Griffin"));

        harness.assertNotOnBattlefield(player1, "Diving Griffin");
        harness.assertInGraveyard(player1, "Diving Griffin");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Searing Wind deals no damage when its only target leaves the battlefield")
    void doesNotResolveWhenTargetIsReturnedToHand() {
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new SearingWind()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.BLUE, 2);
        UUID targetId = harness.getPermanentId(player2, "Diving Griffin");

        harness.castInstant(player1, 0, targetId);
        gs.passPriority(gd, player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Diving Griffin");
        harness.assertNotInGraveyard(player2, "Diving Griffin");
        harness.assertInGraveyard(player1, "Searing Wind");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
