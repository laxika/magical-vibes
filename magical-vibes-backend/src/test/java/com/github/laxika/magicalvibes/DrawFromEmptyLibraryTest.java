package com.github.laxika.magicalvibes;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class DrawFromEmptyLibraryTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Player loses when drawing from an empty library (CR 704.5b)")
    void playerLosesWhenDrawingFromEmptyLibrary() {
        // Empty player1's library
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        // Set up so player1 is the active player on turn 2+ (draw step draws a card)
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        // Pass priorities through upkeep to advance to draw step
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Player does NOT lose when they still have cards in library")
    void playerDoesNotLoseWithCardsInLibrary() {
        // Ensure player1 has cards in library (default setup already has cards)
        assertThat(gd.playerDecks.get(player1.getId())).isNotEmpty();

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Player with Platinum Angel does NOT lose when drawing from empty library")
    void platinumAngelPreventsLossFromEmptyLibrary() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Player loses after Platinum Angel is removed and draws from empty library")
    void playerLosesAfterPlatinumAngelRemovedAndDrawsFromEmpty() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        // First, draw from empty with Platinum Angel — should survive
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        // Remove Platinum Angel
        gd.playerBattlefields.get(player1.getId()).clear();

        // Now draw from empty again — should lose
        gd.turnNumber = 3;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
