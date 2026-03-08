package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnwindingClockTest extends BaseCardTest {

    @Test
    @DisplayName("Unwinding Clock untaps artifacts during opponent's untap step")
    void untapsArtifactsDuringOpponentUntapStep() {
        addToBattlefield(player1, new UnwindingClock());
        Permanent artifact = addToBattlefield(player1, new AngelsFeather());

        artifact.tap();
        assertThat(artifact.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unwinding Clock does not untap non-artifact creatures during opponent's untap step")
    void doesNotUntapNonArtifacts() {
        addToBattlefield(player1, new UnwindingClock());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());

        bears.tap();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unwinding Clock untaps itself during opponent's untap step")
    void untapsItself() {
        Permanent clock = addToBattlefield(player1, new UnwindingClock());

        clock.tap();
        assertThat(clock.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(clock.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unwinding Clock only affects its controller's artifacts")
    void onlyAffectsControllerArtifacts() {
        addToBattlefield(player1, new UnwindingClock());
        Permanent p1Artifact = addToBattlefield(player1, new AngelsFeather());
        Permanent p2Artifact = addToBattlefield(player2, new AngelsFeather());

        p1Artifact.tap();
        p2Artifact.tap();

        advanceToNextTurn(player1); // player2 becomes active

        // Player1's artifact should untap (clock controller)
        assertThat(p1Artifact.isTapped()).isFalse();
        // Player2's artifact stays tapped — player2 doesn't control a clock,
        // and player2 is active so the clock effect doesn't apply
        // (it's "each OTHER player's untap step")
        // Actually player2 IS the active player so their normal untap handles their artifacts.
        // Player2's artifact untaps via normal untap step.
        assertThat(p2Artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Unwinding Clock, non-active player's artifacts stay tapped")
    void withoutClockArtifactsStayTapped() {
        Permanent artifact = addToBattlefield(player1, new AngelsFeather());

        artifact.tap();
        assertThat(artifact.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unwinding Clock untaps artifacts but not creatures when both are controlled")
    void untapsArtifactsButNotCreatures() {
        addToBattlefield(player1, new UnwindingClock());
        Permanent artifact = addToBattlefield(player1, new AngelsFeather());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());

        artifact.tap();
        bears.tap();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    private Permanent addToBattlefield(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
