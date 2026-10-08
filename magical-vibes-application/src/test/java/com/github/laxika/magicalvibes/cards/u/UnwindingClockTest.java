package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BlindZealot;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnwindingClock.class, PristineTalisman.class, BlindZealot.class, PorcelainLegionnaire.class})
class UnwindingClockTest extends BaseCardTest {

    @Test
    @DisplayName("Unwinding Clock untaps artifacts during opponent's untap step")
    void untapsArtifactsDuringOpponentUntapStep() {
        addCreatureReady(player1, new UnwindingClock());
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());

        artifact.tap();
        assertThat(artifact.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unwinding Clock does not untap non-artifact creatures during opponent's untap step")
    void doesNotUntapNonArtifacts() {
        addCreatureReady(player1, new UnwindingClock());
        Permanent bears = addCreatureReady(player1, new BlindZealot());

        bears.tap();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unwinding Clock untaps itself during opponent's untap step")
    void untapsItself() {
        Permanent clock = addCreatureReady(player1, new UnwindingClock());

        clock.tap();
        assertThat(clock.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(clock.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Clock controller's artifacts and active player's artifacts both untap")
    void onlyAffectsControllerArtifacts() {
        addCreatureReady(player1, new UnwindingClock());
        Permanent p1Artifact = addCreatureReady(player1, new PristineTalisman());
        Permanent p2Artifact = addCreatureReady(player2, new PristineTalisman());

        p1Artifact.tap();
        p2Artifact.tap();

        advanceToNextTurn(player1); // player2 becomes active

        assertThat(p1Artifact.isTapped()).isFalse();
        assertThat(p2Artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Unwinding Clock, non-active player's artifacts stay tapped")
    void withoutClockArtifactsStayTapped() {
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());

        artifact.tap();
        assertThat(artifact.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unwinding Clock untaps artifacts but not creatures when both are controlled")
    void untapsArtifactsButNotCreatures() {
        addCreatureReady(player1, new UnwindingClock());
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());
        Permanent bears = addCreatureReady(player1, new BlindZealot());

        artifact.tap();
        bears.tap();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures untap without losing summoning sickness on another player's turn")
    void untapsArtifactCreaturesWithoutRemovingSummoningSickness() {
        harness.addToBattlefield(player1, new UnwindingClock());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        creature.setSummoningSick(true);
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Clock does not untap the nonactive player's artifacts")
    void opponentsClockDoesNotUntapNonactivePlayersArtifacts() {
        harness.addToBattlefield(player2, new UnwindingClock());
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());
        artifact.tap();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Clock untaps artifacts during every opposing untap step")
    void untapsDuringEachOpposingUntapStep() {
        harness.addToBattlefield(player1, new UnwindingClock());
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());
        artifact.tap();

        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
        artifact.tap();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Clock's static ability stops applying when it leaves the battlefield")
    void noUntapAfterClockLeavesBattlefield() {
        Permanent clock = addCreatureReady(player1, new UnwindingClock());
        Permanent artifact = addCreatureReady(player1, new PristineTalisman());
        gd.playerBattlefields.get(player1.getId()).remove(clock);
        artifact.tap();

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
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
