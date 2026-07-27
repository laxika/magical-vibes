package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeTest extends BaseCardTest {

    @Test
    @DisplayName("Only the one chosen creature untaps; other creatures stay tapped, non-creatures untap normally")
    void onlyOneCreatureUntaps() {
        addCreatureReady(player1, new Smoke());
        Permanent bearsA = addCreatureReady(player1, new GrizzlyBears());
        Permanent bearsB = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        bearsA.tap();
        bearsB.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsA.getId()));

        assertThat(bearsA.isTapped()).isFalse();
        assertThat(bearsB.isTapped()).isTrue();
        // Smoke caps only creatures — the land untaps normally regardless of the choice.
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One or fewer creatures untap normally without a choice")
    void oneCreatureUntapsNormally() {
        addCreatureReady(player1, new Smoke());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Smoke restricts your untap step too")
    void opponentSmokeRestrictsYourUntap() {
        addCreatureReady(player2, new Smoke());
        Permanent bearsA = addCreatureReady(player1, new GrizzlyBears());
        Permanent bearsB = addCreatureReady(player1, new GrizzlyBears());
        bearsA.tap();
        bearsB.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsB.getId()));

        assertThat(bearsA.isTapped()).isTrue();
        assertThat(bearsB.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
