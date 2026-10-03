package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.cards.n.NefariousLich;
import com.github.laxika.magicalvibes.cards.s.SwarmIntelligence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApproachOfTheSecondSun.class, HazeOfPollen.class})
class ApproachOfTheSecondSunTest extends BaseCardTest {

    @Test
    @DisplayName("First cast: gains 7 life and goes seventh from the top of the library")
    void firstCastGainsLifeAndTucksSeventhFromTop() {
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        // Six filler cards so "seventh from the top" is a real position.
        harness.setLibrary(player1, List.of(new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen(),
                new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 7);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.get(6)).isSameAs(approach);
    }

    @Test
    @DisplayName("Wins the game when casting a second same-named spell from hand this game")
    void secondCastFromHandWinsGame() {
        // A prior Approach was cast earlier this game.
        gd.recordSpellCast(player1.getId(), new ApproachOfTheSecondSun());

        harness.castFromHand(player1, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A single Approach cast this game does not win even from hand")
    void singleCastDoesNotWin() {
        harness.setLibrary(player1, List.of(new HazeOfPollen()));

        harness.castFromHand(player1, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void emptyLibraryReceivesApproachOnTopAndGainsLife() {
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(approach);
        harness.assertLife(player1, 27);
        harness.assertNotInGraveyard(player1, "Approach of the Second Sun");
    }

    @Test
    void shortLibraryReceivesApproachOnBottom() {
        Card filler = new HazeOfPollen();
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.setLibrary(player1, List.of(filler));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler, approach);
        harness.assertLife(player1, 27);
    }

    @Test
    void longerLibraryPreservesCardsBelowSeventhPosition() {
        List<Card> fillers = List.of(new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen(),
                new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen());
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.setLibrary(player1, fillers);

        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                fillers.get(0), fillers.get(1), fillers.get(2), fillers.get(3),
                fillers.get(4), fillers.get(5), approach, fillers.get(6));
    }

    @Test
    void opponentsPriorCastDoesNotSatisfyWinCondition() {
        gd.recordSpellCast(player2.getId(), new ApproachOfTheSecondSun());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 27);
    }

    @Test
    @CardUsed({Cancel.class})
    void counteredFirstCastStillCountsForWinningSecondCast() {
        ApproachOfTheSecondSun first = new ApproachOfTheSecondSun();
        harness.castFromHand(player1, first, "{6}{W}");
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Approach of the Second Sun");

        harness.castFromHand(player1, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void castFromExileCannotWinEvenAfterPriorCast() {
        gd.recordSpellCast(player1.getId(), new ApproachOfTheSecondSun());
        ApproachOfTheSecondSun exiled = new ApproachOfTheSecondSun();
        harness.setExile(player1, List.of(exiled));
        gd.exilePlayPermissions.put(exiled.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 27);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(exiled);
    }

    @Test
    void firstCastFromExileCountsForLaterCastFromHand() {
        ApproachOfTheSecondSun exiled = new ApproachOfTheSecondSun();
        harness.setExile(player1, List.of(exiled));
        gd.exilePlayPermissions.put(exiled.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.castFromHand(player1, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void drawingAndRecastingSamePhysicalCardWins() {
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @CardUsed({SwarmIntelligence.class})
    void copyingFirstCastDoesNotCountAsAnotherCast() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();

        harness.castFromHand(player1, approach, "{6}{W}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 27);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 34);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(approach);
    }

    @Test
    @CardUsed({SwarmIntelligence.class, Cancel.class})
    void copyOfSecondCastCannotWinWhenOriginalIsCountered() {
        gd.recordSpellCast(player1.getId(), new ApproachOfTheSecondSun());
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.castFromHand(player1, approach, "{6}{W}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, approach.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Approach of the Second Sun");
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 27);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({NefariousLich.class})
    void approachEntersLibraryBeforeLifeGainIsReplacedWithSevenDraws() {
        harness.addToBattlefield(player1, new NefariousLich());
        List<Card> fillers = List.of(new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen(),
                new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen());
        harness.setLibrary(player1, fillers);
        harness.setLife(player1, 20);
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();

        harness.castFromHand(player1, approach, "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                fillers.get(0), fillers.get(1), fillers.get(2), fillers.get(3),
                fillers.get(4), fillers.get(5), approach);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fillers.get(6));
    }
}
