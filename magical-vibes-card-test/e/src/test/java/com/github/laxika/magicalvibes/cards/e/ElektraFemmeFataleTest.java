package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;


@CardUsed({ElektraFemmeFatale.class})
class ElektraFemmeFataleTest extends BaseCardTest {

    @Test
    void acceptingSelfDamageDealsFourDamageToAChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElektraFemmeFatale());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ElektraFemmeFatale(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Elektra, Femme Fatale");
        harness.assertInGraveyard(player2, "Elektra, Femme Fatale");
    }

    @Test
    void decliningSelfDamageDoesNotDealDamageToACreature() {
        harness.addToBattlefield(player2, new ElektraFemmeFatale());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ElektraFemmeFatale(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Elektra, Femme Fatale");
    }

    @Test
    void canChooseHerselfAndDamageWaitsForTheReflexiveAbilityToResolve() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ElektraFemmeFatale(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Elektra, Femme Fatale"));

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Elektra, Femme Fatale");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elektra, Femme Fatale");
        harness.assertInGraveyard(player1, "Elektra, Femme Fatale");
    }
}
