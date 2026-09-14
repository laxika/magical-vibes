package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellOfIdeas.class})
class WellOfIdeasTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield draws two cards")
    void enteringTheBattlefieldDrawsTwoCards() {
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new WellOfIdeas());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Controller draws two additional cards during their draw step")
    void controllerDrawsTwoAdditionalCards() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Each opponent draws one additional card during their draw step")
    void opponentDrawsOneAdditionalCard() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }
}
