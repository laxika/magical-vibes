package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoardersGreedTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HoardersGreed()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {3}{B}
        // Opponent always reveals a mana-value-0 card, so the caster wins only when their own
        // revealed card has a strictly greater mana value.
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Losing the first clash runs the process exactly once")
    void losingFirstClashRunsOnce() {
        prepare();
        // Draw removes the top two Forests; the clash then reveals a Forest (MV 0) → tie, caster loses.
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18); // one iteration: lost 2 life
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2); // drew two cards
    }

    @Test
    @DisplayName("Winning a clash repeats the whole process until a clash is lost")
    void winningRepeatsUntilLoss() {
        prepare();
        // Iteration 1: draw the two leading Forests, then reveal Grizzly Bears (MV 2) → win, repeat.
        // Iteration 2: draw the Grizzly Bears + a Forest, then reveal a Forest (MV 0) → tie, loss, stop.
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16); // two iterations: lost 4 life
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4); // drew four cards total
    }
}
