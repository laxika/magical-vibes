package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordJyscalGuado.class, GrizzlyBears.class, TimberlandGuide.class})
class LordJyscalGuadoTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates at each end step after its controller puts a counter on a creature")
    void investigatesAfterControllerPutsCounterOnCreature() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when no counter was put on a creature")
    void doesNotInvestigateWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new LordJyscalGuado());

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A counter put by another player does not satisfy the condition")
    void opponentCounterPlacementDoesNotInvestigate() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
