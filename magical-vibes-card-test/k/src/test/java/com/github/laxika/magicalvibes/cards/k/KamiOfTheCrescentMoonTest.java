package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfTheCrescentMoon.class})
class KamiOfTheCrescentMoonTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
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

    @Test
    @DisplayName("The extra draw resolves after the normal draw")
    void extraDrawWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("A tapped Kami still grants the additional draw")
    void tappedKamiStillTriggers() {
        harness.addToBattlefieldAndReturn(player1, new KamiOfTheCrescentMoon()).tap();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("The additional draw still resolves after Kami leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Kamis controlled by different players each grant an additional draw")
    void bothPlayersKamisTrigger() {
        harness.addToBattlefield(player1, new KamiOfTheCrescentMoon());
        harness.addToBattlefield(player2, new KamiOfTheCrescentMoon());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int otherHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(otherHandBefore);
    }
}
