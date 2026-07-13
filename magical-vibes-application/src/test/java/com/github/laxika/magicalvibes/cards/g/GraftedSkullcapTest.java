package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraftedSkullcapTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance from UPKEEP to DRAW, trigger onto stack
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Draw step draws an additional card")
    void drawStepDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve draw trigger

        // Normal draw (1) + additional draw (1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Extra card only on controller's draw step, not opponent's")
    void extraCardOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player2, List.of());

        advanceToDraw(player2);

        // Only the normal draw — no trigger on the opponent's draw step
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("End step discards the controller's entire hand")
    void endStepDiscardsHand() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves()));

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Llanowar Elves"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Discard only on controller's end step, not opponent's")
    void discardOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player2, List.of(new LlanowarElves()));

        advanceToEndStepTrigger(player2);

        // Opponent's hand is untouched by the controller's Skullcap
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
