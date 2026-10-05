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
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.l.LivingPlane;
import com.github.laxika.magicalvibes.cards.p.PsychicPaper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazesEnd.class, AzoriusGuildgate.class, BorosGuildgate.class, DimirGuildgate.class,
        GolgariGuildgate.class, GruulGuildgate.class, IzzetGuildgate.class, OrzhovGuildgate.class,
        RakdosGuildgate.class, SelesnyaGuildgate.class, SimicGuildgate.class, KraulWarrior.class,
        LivingPlane.class, PsychicPaper.class})
class MazesEndTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces colorless mana")
    void entersTappedAndProducesColorlessMana() {
        harness.setHand(player1, List.of(new MazesEnd()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

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
        harness.setLibrary(player1, List.of(new SimicGuildgate(), new KraulWarrior()));

        activateMaze(maze);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Maze's End");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Simic Guildgate");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Simic Guildgate");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins after the search produces the tenth differently named Gate")
    void winsWithTenDifferentGatesAfterSearch() {
        addDistinctGatesExceptSimic();
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new SimicGuildgate()));

        activateMaze(maze);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Duplicate Gate names do not satisfy the win condition")
    void duplicateGateNamesDoNotWin() {
        addDistinctGatesExceptSimic();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new AzoriusGuildgate()));

        activateMaze(maze);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Checks the win condition even when no Gate is found")
    void winsWithoutFindingAnotherGateWhenAlreadyAtTen() {
        addAllDistinctGates();
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new KraulWarrior()));

        activateMaze(maze);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Returns Maze's End and pays mana before the ability resolves")
    void paysCostsBeforeResolution() {
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new SimicGuildgate()));

        activateMaze(maze);

        harness.assertInHand(player1, "Maze's End");
        harness.assertNotOnBattlefield(player1, "Maze's End");
        harness.assertNotOnBattlefield(player1, "Simic Guildgate");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Simic Guildgate").isTapped()).isTrue();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        harness.assertOnBattlefield(player1, "Maze's End");
    }

    @Test
    @DisplayName("Can decline to find an available Gate and still win")
    void winsAfterDecliningAvailableGate() {
        addAllDistinctGates();
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new SimicGuildgate()));

        activateMaze(maze);
        harness.passBothPriorities();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Simic Guildgate");
    }

    @Test
    @DisplayName("Checks the win condition when the library is empty")
    void winsWithEmptyLibrary() {
        addAllDistinctGates();
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of());

        activateMaze(maze);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An opponent's tenth Gate does not count toward the win")
    void opponentsGateDoesNotCount() {
        addDistinctGatesExceptSimic();
        harness.addToBattlefield(player2, new SimicGuildgate());
        Permanent maze = addMazeReady();
        harness.setLibrary(player1, List.of(new AzoriusGuildgate()));

        activateMaze(maze);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Gates given the same current name do not satisfy the win condition")
    void renamedGatesWithSameNameDoNotWin() {
        addAllDistinctGates();
        harness.addToBattlefield(player1, new LivingPlane());
        harness.addToBattlefield(player1, new KraulWarrior());
        for (String gateName : List.of("Azorius Guildgate", "Simic Guildgate")) {
            Permanent gate = findPermanent(player1, gateName);
            Permanent paper = harness.addToBattlefieldAndReturn(player1, new PsychicPaper());
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(paper),
                    null, gate.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleListChoice(player1, "Kraul Warrior");
            harness.handleListChoice(player1, "INSECT");
            assertThat(gqs.getEffectiveName(gd, gate)).isEqualTo("Kraul Warrior");
        }
        Permanent maze = addMazeReady();
        maze.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new KraulWarrior()));

        activateMaze(maze);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
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
