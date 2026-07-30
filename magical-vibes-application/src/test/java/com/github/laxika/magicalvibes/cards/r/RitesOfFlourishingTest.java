package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RitesOfFlourishingTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Controller draws one additional card during their draw step")
    void triggersDrawForController() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Opponent draws one additional card during their own draw step, controller draws nothing")
    void triggersDrawForOpponentOnly() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 2);
    }

    @Test
    @DisplayName("Each player may play one additional land per turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Two copies stack for both the extra draw and the extra land play")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        harness.addToBattlefield(player2, new RitesOfFlourishing());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(3);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }
}
