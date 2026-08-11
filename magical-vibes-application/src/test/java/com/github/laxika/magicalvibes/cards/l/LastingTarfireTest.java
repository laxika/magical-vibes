package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class LastingTarfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each opponent at end step after its controller puts a counter on a creature")
    void dealsDamageAfterControllerPutsCounterOnCreature() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger at end step when its controller did not put a counter on a creature")
    void doesNotDealDamageWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new LastingTarfire());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A counter put by another player does not satisfy the condition")
    void opponentCounterPlacementDoesNotTrigger() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
