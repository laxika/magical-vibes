package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootMaze.class, Forest.class, Ornithopter.class, GrizzlyBears.class})
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
}
