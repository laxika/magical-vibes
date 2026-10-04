package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MindwrackHarpy.class)
class MindwrackHarpyTest extends BaseCardTest {

    @Test
    void eachPlayerMillsThreeAtBeginningOfCombatOnYourTurn() {
        harness.addToBattlefield(player1, new MindwrackHarpy());
        int player1LibraryBefore = gd.playerDecks.get(player1.getId()).size();
        int player2LibraryBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToBeginningOfCombat(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1LibraryBefore - 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2LibraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void doesNotTriggerAtBeginningOfCombatOnOpponentsTurn() {
        harness.addToBattlefield(player1, new MindwrackHarpy());
        int player1LibraryBefore = gd.playerDecks.get(player1.getId()).size();
        int player2LibraryBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToBeginningOfCombat(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1LibraryBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2LibraryBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
