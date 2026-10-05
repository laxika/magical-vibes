package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.SharedFate;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaboratoryManiac.class, PlatinumAngel.class, SharedFate.class, ThinkTwice.class, TurnToFrog.class})
class LaboratoryManiacTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the last card does not win; the next draw from the empty library does")
    void winsOnlyOnDrawAfterLastCard() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        ThinkTwice lastCard = new ThinkTwice();
        harness.setLibrary(player1, List.of(lastCard));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(lastCard);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Laboratory Maniac cannot replace an empty-library draw after losing its abilities")
    void losesWhenManiacHasLostAbilities() {
        var maniac = harness.addToBattlefieldAndReturn(player1, new LaboratoryManiac());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TurnToFrog(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, maniac.getId());
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Drawing player chooses between Laboratory Maniac and Shared Fate replacements")
    void competingReplacementRequiresDrawingPlayersChoice() {
        harness.addToBattlefield(player1, new LaboratoryManiac());
        harness.addToBattlefield(player2, new SharedFate());
        harness.setLibrary(player1, List.of());
        ThinkTwice opponentTopCard = new ThinkTwice();
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
    }

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains(gd.playerIdToName.get(player1.getId())) && l.contains("wins the game"));
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains(gd.playerIdToName.get(player2.getId())) && l.contains("wins the game"));
    }
}
