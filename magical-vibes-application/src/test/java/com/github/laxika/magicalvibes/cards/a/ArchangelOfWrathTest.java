package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed(ArchangelOfWrath.class)
class ArchangelOfWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Without either kicker, it deals no damage")
    void noKicker() {
        harness.setHand(player1, List.of(new ArchangelOfWrath()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Black kicker deals 2 damage")
    void blackKickerDealsDamage() {
        harness.setHand(player1, List.of(new ArchangelOfWrath()));
        addMana(ManaColor.BLACK);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Red kicker deals 2 damage")
    void redKickerDealsDamage() {
        harness.setHand(player1, List.of(new ArchangelOfWrath()));
        addMana(ManaColor.RED);

        castWithAdditionalCosts(List.of("{R}"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Both kicker costs deal 2 damage each")
    void bothKickersDealFourDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchangelOfWrath()));
        addMana(ManaColor.BLACK, ManaColor.RED);

        castWithAdditionalCosts(List.of("{R}"), player2.getId(), true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addMana(ManaColor... additionalCosts) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (ManaColor color : additionalCosts) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, false);
    }

    private void castWithAdditionalCosts(List<String> payments, UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
