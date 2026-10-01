package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtReflection.class, Forest.class, Island.class, Mountain.class})
class ThoughtReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("A single draw draws two cards instead for the controller")
    void doublesControllerDraw() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new Island(),
                new Mountain()));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Mountain");
    }

    @Test
    @DisplayName("Only the controller's draws are doubled, not an opponent's")
    void doesNotDoubleOpponentDraw() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player2, List.of(
                new Forest(),
                new Mountain()));
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Mountain");
    }

    @Test
    @DisplayName("A controller's first draw during their draw step is also doubled")
    void doublesDrawStepDraw() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two Thought Reflections each replace the draw")
    void multipleReflectionsStack() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Forest()));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
