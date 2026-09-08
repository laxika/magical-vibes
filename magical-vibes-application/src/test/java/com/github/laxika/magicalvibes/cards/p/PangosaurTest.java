package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PangosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to its owner's hand when its controller plays a land")
    void returnsWhenControllerPlaysLand() {
        harness.addToBattlefield(player1, new Pangosaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pangosaur");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Returns to its owner's hand when an opponent plays a land")
    void returnsWhenOpponentPlaysLand() {
        harness.addToBattlefield(player1, new Pangosaur());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pangosaur");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when a land enters the battlefield without being played")
    void doesNotReturnWhenLandEntersWithoutBeingPlayed() {
        harness.addToBattlefield(player1, new Pangosaur());
        harness.addToBattlefield(player2, new Forest());

        harness.assertOnBattlefield(player1, "Pangosaur");
    }
}
