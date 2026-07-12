package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PucasMischiefTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Exchanges control of both permanents when accepted")
    void exchangesControlWhenAccepted() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // MV 2
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());  // MV 1

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());  // nonland permanent you control
        harness.handlePermanentChosen(player1, opp.getId());  // opponent's permanent (MV <= own)
        harness.passBothPriorities();                         // resolve trigger to the "may" prompt
        harness.handleMayAbilityChosen(player1, true);        // accept the exchange

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("No exchange when the controller declines the may ability")
    void noExchangeWhenDeclined() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);       // decline

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not trigger a choice when no legal target pair exists")
    void noLegalPairDoesNothing() {
        // Player1's nonland permanents (Puca's Mischief MV 4, Grizzly Bears MV 2) are all below the
        // only opponent permanent's mana value (Air Elemental MV 5), so no legal pair can be chosen.
        harness.addToBattlefield(player1, new PucasMischief());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new AirElemental());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Exchange fizzles when a target leaves the battlefield before resolution")
    void exchangeFizzlesWhenTargetGone() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opp.getId());
        // Opponent's target leaves the battlefield before the ability resolves.
        gd.playerBattlefields.get(player2.getId()).remove(opp);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // No exchange happened — the controller keeps their permanent.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
