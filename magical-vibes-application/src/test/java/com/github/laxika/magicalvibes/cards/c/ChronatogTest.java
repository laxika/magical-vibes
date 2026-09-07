package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Chronatog.class)
class ChronatogTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +3/+3 until end of turn")
    void pumpsUntilEndOfTurn() {
        Permanent chronatog = addCreatureReady(player1, new Chronatog());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        assertThat(chronatog.getEffectivePower()).isEqualTo(4);
        assertThat(chronatog.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(chronatog.getEffectivePower()).isEqualTo(1);
        assertThat(chronatog.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability queues a skip of the controller's next turn")
    void queuesSkipNextTurn() {
        Permanent chronatog = addCreatureReady(player1, new Chronatog());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Queued skip causes the controller's next turn to be skipped")
    void skipsNextTurn() {
        Permanent chronatog = addCreatureReady(player1, new Chronatog());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        // End player1's turn → player2's turn begins normally.
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // End player2's turn → player1 would act but is skipped, so player2 acts again.
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent chronatog = addCreatureReady(player1, new Chronatog());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chronatog), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Once-per-turn restriction resets when the controller gets a later turn")
    void canActivateAgainAfterSkippedTurn() {
        Permanent chronatog = addCreatureReady(player1, new Chronatog());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        // Player 1's next turn is skipped; the following turn is theirs again.
        advanceTurn(player2);
        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(chronatog), null, null);
        harness.passBothPriorities();

        assertThat(chronatog.getEffectivePower()).isEqualTo(4);
        assertThat(chronatog.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceTurn(Player expectedActivePlayer) {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(expectedActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
