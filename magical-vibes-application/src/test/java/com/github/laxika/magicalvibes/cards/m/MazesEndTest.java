package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.g.GolgariGuildgate;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.i.IzzetGuildgate;
import com.github.laxika.magicalvibes.cards.o.OrzhovGuildgate;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MazesEndTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces colorless mana")
    void entersTappedAndProducesColorlessMana() {
        harness.setHand(player1, List.of(new MazesEnd()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

        Permanent maze = findPermanent(player1, "Maze's End");

        assertThat(maze.isTapped()).isTrue();

        maze.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns itself to hand and searches for a Gate onto the battlefield")
    void returnsItselfAndSearchesForGate() {
        Permanent maze = addMazeReady();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new SimicGuildgate(), new GrizzlyBears()));

        activateMaze(maze);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Maze's End");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Simic Guildgate");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Simic Guildgate");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins after the search produces the tenth differently named Gate")
    void winsWithTenDifferentGatesAfterSearch() {
        addDistinctGatesExceptSimic();
        Permanent maze = addMazeReady();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new SimicGuildgate());

        activateMaze(maze);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Duplicate Gate names do not satisfy the win condition")
    void duplicateGateNamesDoNotWin() {
        addDistinctGatesExceptSimic();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        Permanent maze = addMazeReady();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new AzoriusGuildgate());

        activateMaze(maze);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Checks the win condition even when no Gate is found")
    void winsWithoutFindingAnotherGateWhenAlreadyAtTen() {
        addAllDistinctGates();
        Permanent maze = addMazeReady();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        activateMaze(maze);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addMazeReady() {
        Permanent maze = harness.addToBattlefieldAndReturn(player1, new MazesEnd());
        maze.untap();
        return maze;
    }

    private void activateMaze(Permanent maze) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(maze), null, null);
    }

    private void addDistinctGatesExceptSimic() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new DimirGuildgate());
        harness.addToBattlefield(player1, new GolgariGuildgate());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.addToBattlefield(player1, new OrzhovGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());
    }

    private void addAllDistinctGates() {
        addDistinctGatesExceptSimic();
        harness.addToBattlefield(player1, new SimicGuildgate());
    }
}
