package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YawgmothsBargain.class})
class YawgmothsBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life draws a card and the ability can be activated repeatedly")
    void paysLifeToDrawCards() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain(), new YawgmothsBargain()));
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.setLife(player1, 20);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain()));
        harness.addToBattlefield(player1, new YawgmothsBargain());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("An opponent's Bargain does not skip the active player's draw step")
    void opponentBargainDoesNotSkipActivePlayersDrawStep() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain()));
        harness.addToBattlefield(player2, new YawgmothsBargain());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Skipping the draw step proceeds directly from upkeep to the main phase")
    void skipsEntireDrawStep() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain()));
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Life is paid on activation and the card is drawn only on resolution")
    void paysLifeBeforeDrawing() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain()));
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay life to draw during an opponent's turn")
    void drawsOnOpponentsTurn() {
        harness.setLibrary(player1, List.of(new YawgmothsBargain()));
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
