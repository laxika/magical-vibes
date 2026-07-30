package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TouchOfTheEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger sets life to the number of permanents you control")
    void upkeepSetsLifeToPermanentCount() {
        addTouch(player1);
        addBears(player1);
        addBears(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        runUpkeep(player1);

        harness.assertLife(player1, 3); // the enchantment itself plus two Bears
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Permanents controlled by the opponent are not counted")
    void opponentPermanentsAreNotCounted() {
        addTouch(player1);
        addBears(player2);
        addBears(player2);
        addBears(player2);
        harness.setLife(player1, 20);

        runUpkeep(player1);

        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Life can go up as well as down")
    void lifeCanIncrease() {
        addTouch(player1);
        for (int i = 0; i < 9; i++) {
            addBears(player1);
        }
        harness.setLife(player1, 4);

        runUpkeep(player1);

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("The trigger does not fire during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addTouch(player1);
        addBears(player1);
        harness.setLife(player1, 20);

        runUpkeep(player2);

        harness.assertLife(player1, 20);
    }

    private Permanent addTouch(Player owner) {
        Permanent perm = new Permanent(new TouchOfTheEternal());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void addBears(Player owner) {
        gd.playerBattlefields.get(owner.getId()).add(new Permanent(new GrizzlyBears()));
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve it
    }
}
