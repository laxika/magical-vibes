package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JourneyersKite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampingField.class, JourneyersKite.class, Forest.class})
class DampingFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Only one artifact untaps; non-artifacts untap normally")
    void onlyOneArtifactUntaps() {
        harness.addToBattlefield(player1, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstKite.tap();
        secondKite.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Damping Field still imposes the restriction")
    void tappedDampingFieldStillRestricts() {
        Permanent dampingField = harness.addToBattlefieldAndReturn(player1, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        dampingField.tap();
        firstKite.tap();
        secondKite.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
        assertThat(dampingField.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Damping Field restricts your untap step")
    void opponentDampingFieldRestricts() {
        harness.addToBattlefield(player2, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        firstKite.tap();
        secondKite.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
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
