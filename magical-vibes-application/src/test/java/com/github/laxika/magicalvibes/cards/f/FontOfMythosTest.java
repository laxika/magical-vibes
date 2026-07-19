package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FontOfMythosTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Active player draws two additional cards during their draw step")
    void triggersDrawForActivePlayer() {
        harness.addToBattlefield(player1, new FontOfMythos());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Font of Mythos trigger

        // Normal draw + two additional = 3 total
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Opponent draws two additional cards during their draw step")
    void triggersDrawForOpponent() {
        harness.addToBattlefield(player1, new FontOfMythos());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Font of Mythos trigger

        // Normal draw + two additional = 3 total
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Only the active player draws extra, not the controller")
    void onlyActivePlayerDrawsExtra() {
        harness.addToBattlefield(player1, new FontOfMythos());
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Font of Mythos trigger

        // Player1 (controller) should NOT draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Player2 (active player) draws normal + 2 extra
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 3);
    }

    @Test
    @DisplayName("Does not trigger on first turn for the starting player (draw step is skipped)")
    void doesNotTriggerOnFirstTurn() {
        harness.addToBattlefield(player1, new FontOfMythos());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        gd.turnNumber = 1;
        gd.startingPlayerId = player1.getId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW — but entire step is skipped

        // No draws at all — entire draw step skipped per rule 103.7a
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
