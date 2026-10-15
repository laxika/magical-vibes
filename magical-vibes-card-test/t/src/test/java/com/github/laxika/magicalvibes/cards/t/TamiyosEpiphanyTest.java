package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TamiyosEpiphany.class, Forest.class, Island.class, Mountain.class, Plains.class,
        GrizzlyBears.class, LlanowarElves.class})
class TamiyosEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Scries four, then draws two cards")
    void scriesFourThenDrawsTwo() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(forest, island, mountain, plains, bears, elves));
        castTamiyosEpiphany();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(forest, island, mountain, plains);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elves, forest, mountain, plains);
    }

    @Test
    @DisplayName("Drawing from an empty library loses the game")
    void emptyLibraryLosesTheGame() {
        harness.setLibrary(player1, List.of());
        castTamiyosEpiphany();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Keeping all four cards allows reordering before drawing")
    void reordersAllFourOnTopBeforeDrawing() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, island, mountain, plains));
        castTamiyosEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(3, 2, 1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        harness.assertInGraveyard(player1, "Tamiyo's Epiphany");
    }

    @Test
    @DisplayName("Scrying a short library still draws bottomed cards")
    void scriesOnlyAvailableCardsAndDrawsFromBottom() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        castTamiyosEpiphany();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(forest, island);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void castTamiyosEpiphany() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TamiyosEpiphany(), "{3}{U}");
        harness.passBothPriorities();
    }
}
