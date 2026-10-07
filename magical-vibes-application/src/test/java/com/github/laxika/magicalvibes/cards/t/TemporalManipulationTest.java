package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemporalManipulation.class})
class TemporalManipulationTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private TemporalManipulation cast() {
        TemporalManipulation card = new TemporalManipulation();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, card, "{3}{U}{U}");
        harness.passBothPriorities();
        return card;
    }

    @Test
    @DisplayName("Resolving queues one extra turn for the caster")
    void resolvingQueuesOneExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            cast();

            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }

    @Test
    @DisplayName("The extra turn is taken by the caster after the current turn ends")
    void extraTurnTakenByCaster() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            int turnBefore = gd.turnNumber;
            cast();

            advanceTurn();

            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
            assertThat(gd.extraTurns).isEmpty();
        });
    }

    @Test
    @DisplayName("Normal turn order resumes after the single extra turn")
    void normalTurnOrderResumes() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            int turnBefore = gd.turnNumber;
            cast();

            advanceTurn(); // extra turn
            advanceTurn(); // back to opponent

            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        });
    }

    @Test
    @DisplayName("Temporal Manipulation goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        TemporalManipulation card = cast();

        GameData g = harness.getGameData();
        assertThat(g.playerGraveyards.get(player1.getId()))
                .contains(card);
        assertThat(g.stack).isEmpty();
    }

    @Test
    @DisplayName("Two resolutions grant two consecutive extra turns before the opponent's turn")
    void twoResolutionsGrantTwoExtraTurns() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            cast();
            cast();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.currentTurnIsExtraTurn).isTrue();
            assertThat(gd.extraTurns).containsExactly(player1.getId());

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.currentTurnIsExtraTurn).isTrue();
            assertThat(gd.extraTurns).isEmpty();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.currentTurnIsExtraTurn).isFalse();
        });
    }

    @Test
    @DisplayName("Casting during an extra turn grants another extra turn before normal turns resume")
    void castingDuringExtraTurnGrantsAnotherExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            cast();
            advanceTurn();
            assertThat(gd.currentTurnIsExtraTurn).isTrue();

            cast();
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.currentTurnIsExtraTurn).isTrue();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.currentTurnIsExtraTurn).isFalse();
        });
    }
}
