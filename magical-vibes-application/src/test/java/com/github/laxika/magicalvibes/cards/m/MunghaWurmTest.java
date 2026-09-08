package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MunghaWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Only one land untaps; nonlands untap normally")
    void picksOneLandToUntapNonlandsUntapNormally() {
        addCreatureReady(player1, new MunghaWurm());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        forest.tap();
        mountain.tap();
        bears.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The restriction applies even when Mungha Wurm starts tapped")
    void tappedMunghaWurmStillRestricts() {
        Permanent wurm = addCreatureReady(player1, new MunghaWurm());
        wurm.tap();
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(wurm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Mungha Wurm controlled by an opponent restricts your lands")
    void opponentMunghaWurmRestrictsYourLands() {
        addCreatureReady(player2, new MunghaWurm());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
