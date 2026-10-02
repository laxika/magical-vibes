package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SulfurousBlast.class, SkulkingKnight.class})
class SulfurousBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage during its controller's main phase")
    void dealsThreeDamageDuringMainPhase() {
        harness.addToBattlefield(player1, new SkulkingKnight());
        harness.addToBattlefield(player2, new SkulkingKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SulfurousBlast(), "{2}{R}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertNotOnBattlefield(player2, "Skulking Knight");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage during its controller's postcombat main phase")
    void dealsThreeDamageDuringPostcombatMainPhase() {
        harness.addToBattlefield(player1, new SkulkingKnight());
        harness.addToBattlefield(player2, new SkulkingKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SulfurousBlast(), "{2}{R}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertNotOnBattlefield(player2, "Skulking Knight");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 2 damage when cast outside its controller's main phase")
    void dealsTwoDamageOutsideMainPhase() {
        harness.addToBattlefield(player1, new SkulkingKnight());
        harness.addToBattlefield(player2, new SkulkingKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SulfurousBlast(), "{2}{R}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skulking Knight");
        harness.assertOnBattlefield(player2, "Skulking Knight");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage when cast during an opponent's main phase")
    void dealsTwoDamageDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new SkulkingKnight());
        harness.addToBattlefield(player2, new SkulkingKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SulfurousBlast(), "{2}{R}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skulking Knight");
        harness.assertOnBattlefield(player2, "Skulking Knight");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
