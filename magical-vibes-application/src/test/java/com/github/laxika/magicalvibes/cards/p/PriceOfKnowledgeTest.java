package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriceOfKnowledge.class, Python.class})
class PriceOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to an opponent's hand size on that opponent's upkeep")
    void damagesOpponentEqualToHandSize() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of(new Python(), new Python(), new Python()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player1, List.of(new Python(), new Python(), new Python()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Allows players to keep more than seven cards during cleanup")
    void playersHaveNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Python(), new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python(), new Python())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }
}
