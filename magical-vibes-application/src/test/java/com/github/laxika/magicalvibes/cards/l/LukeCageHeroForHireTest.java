package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LukeCageHeroForHire.class)
class LukeCageHeroForHireTest extends BaseCardTest {

    @Test
    void createsTreasureAtBeginningOfCombatOnYourTurn() {
        harness.addToBattlefield(player1, new LukeCageHeroForHire());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotCreateTreasureAtBeginningOfCombatOnOpponentsTurn() {
        harness.addToBattlefield(player1, new LukeCageHeroForHire());

        advanceToBeginningOfCombat(player2);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
