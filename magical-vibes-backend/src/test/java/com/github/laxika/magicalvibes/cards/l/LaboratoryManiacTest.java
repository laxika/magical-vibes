package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class LaboratoryManiacTest extends BaseCardTest {

    @Test
    @DisplayName("Player wins when drawing from empty library with Laboratory Maniac on the battlefield")
    void winsOnEmptyLibraryDraw() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains(gd.playerIdToName.get(player1.getId())) && l.contains("wins the game"));
    }

    @Test
    @DisplayName("Player still loses from empty library if Laboratory Maniac is not on the battlefield")
    void losesWithoutLaboratoryManiac() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
    }

    @Test
    @DisplayName("Opponent's Laboratory Maniac does not save you from empty library loss")
    void opponentManiacDoesNotHelp() {
        harness.addToBattlefield(player2, new LaboratoryManiac());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
    }

    @Test
    @DisplayName("Game continues when opponent has Platinum Angel — win is blocked but draw is still replaced")
    void platinumAngelBlocksWin() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        harness.addToBattlefield(player2, new PlatinumAngel());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Laboratory Maniac replaces the draw, but Platinum Angel prevents the win
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Player with cards in library draws normally even with Laboratory Maniac")
    void normalDrawWithCardsInLibrary() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        assertThat(gd.playerDecks.get(player1.getId())).isNotEmpty();

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("After Laboratory Maniac is removed, player loses from empty library draw")
    void losesAfterManiacRemoved() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        // Remove Laboratory Maniac before the draw step
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
    }
}
