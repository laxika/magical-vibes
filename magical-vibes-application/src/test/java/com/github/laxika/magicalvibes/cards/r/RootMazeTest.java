package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootMaze.class, Forest.class, Ornithopter.class, GrizzlyBears.class, Naturalize.class})
class RootMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Lands enter tapped while Root Maze is on battlefield")
    void landsEnterTapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifacts enter tapped for both players while Root Maze is on battlefield")
    void artifactsEnterTappedForBothPlayers() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-artifact non-land permanents are not tapped by Root Maze")
    void creaturesAreNotTappedByRootMaze() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Existing permanents are not tapped when Root Maze enters")
    void existingPermanentsAreNotTappedWhenRootMazeEnters() {
        Permanent existingForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new RootMaze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(existingForest.isTapped()).isFalse();
    }
    @Test
    @DisplayName("Opponent lands enter tapped while Root Maze is on battlefield")
    void opponentLandsEnterTapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller artifacts enter tapped while Root Maze is on battlefield")
    void controllerArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Root Maze does not prevent normal untapping")
    void affectedPermanentsUntapNormally() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Forest(), new Ornithopter()));
        harness.playLand(player1, 0);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent forest = findPermanent(player1, "Forest");
        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(forest.isTapped()).isTrue();
        assertThat(ornithopter.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(forest.isTapped()).isFalse();
        assertThat(ornithopter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Root Maze stops affecting entries when destroyed before an artifact resolves")
    void removalBeforeResolutionStopsReplacement() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Ornithopter(), new Naturalize(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castArtifact(player1, 0);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Root Maze").getId());
        harness.assertNotOnBattlefield(player1, "Root Maze");
        harness.assertInGraveyard(player1, "Root Maze");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isFalse();
        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }
}
