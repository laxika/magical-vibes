package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConjuredCurrencyTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of itself and the chosen permanent when accepted")
    void exchangesControlWhenAccepted() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Conjured Currency");
        harness.assertNotOnBattlefield(player1, "Conjured Currency");
    }

    @Test
    @DisplayName("No exchange when the controller declines")
    void noExchangeWhenDeclined() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Permanents its controller controls are not legal targets")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // The only other permanent belongs to the controller, so there is nothing to exchange with.
        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The new controller cannot target a permanent they own but no longer control")
    void newControllerCannotTargetOwnedPermanent() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, elves.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Player 2 now controls Conjured Currency, so on their upkeep it triggers for them. The
        // Llanowar Elves player 1 took are still owned by player 2, so they are not a legal target
        // and nothing can be exchanged back.
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Conjured Currency");
    }
}
