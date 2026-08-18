package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTestHarnessPassUntilTest extends BaseCardTest {

    @Test
    @DisplayName("Passes to the requested step without overshooting it and restores auto-stops")
    void reachesStepAndRestoresAutoStops() {
        Set<TurnStep> player1Stops = new HashSet<>(Set.of(TurnStep.DRAW));
        gd.playerAutoStopSteps.put(player1.getId(), player1Stops);
        Map<UUID, Set<TurnStep>> stopsBefore = copyAutoStops();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(copyAutoStops()).isEqualTo(stopsBefore);
        assertThat(gd.playerAutoStopSteps.get(player1.getId())).isSameAs(player1Stops);
    }

    @Test
    @DisplayName("Player overload passes an earlier occurrence on the other player's turn")
    void reachesStepForRequestedActivePlayer() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        int turnBefore = gd.turnNumber;

        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }

    @Test
    @DisplayName("Fails clearly when advancing requires player input before the target")
    void failsWhenPlayerInputIsRequired() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.passUntil(TurnStep.END_STEP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("input is required")
                .hasMessageContaining("AttackerDeclaration");
    }

    private Map<UUID, Set<TurnStep>> copyAutoStops() {
        Map<UUID, Set<TurnStep>> copy = new HashMap<>();
        gd.playerAutoStopSteps.forEach((playerId, stops) -> copy.put(playerId, new HashSet<>(stops)));
        return copy;
    }
}
