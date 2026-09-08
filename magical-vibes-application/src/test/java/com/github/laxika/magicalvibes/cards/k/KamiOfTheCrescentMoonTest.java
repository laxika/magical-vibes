package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfTheCrescentMoonTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Active player draws an additional card during their draw step")
    void triggersDrawForActivePlayer() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Opponent draws an additional card during their draw step")
    void triggersDrawForOpponent() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Only the active player draws an extra card")
    void onlyActivePlayerDrawsExtra() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int activePlayerHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(activePlayerHandBefore + 2);
    }
}
