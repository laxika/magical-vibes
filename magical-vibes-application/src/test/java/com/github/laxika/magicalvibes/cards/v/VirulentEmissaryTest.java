package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VirulentEmissary.class, GrizzlyBears.class})
class VirulentEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new VirulentEmissary());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when it enters itself")
    void noLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new VirulentEmissary(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotGainLifeForOpponentsCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new VirulentEmissary());

        harness.enterBattlefieldAndReturn(player2, new VirulentEmissary());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachExistingEmissaryTriggersForAnotherEmissary() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new VirulentEmissary());
        harness.addToBattlefield(player1, new VirulentEmissary());

        harness.castFromHand(player1, new VirulentEmissary(), "{G}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsLifeForCreatureEnteringWithoutBeingCast() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new VirulentEmissary());

        harness.enterBattlefieldAndReturn(player1, new VirulentEmissary());
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void deathtouchKillsLargerBlocker() {
        addCreatureReady(player1, new VirulentEmissary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Virulent Emissary");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Virulent Emissary");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
