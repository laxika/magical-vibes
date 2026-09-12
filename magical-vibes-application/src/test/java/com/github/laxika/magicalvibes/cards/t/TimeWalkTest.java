package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TimeWalk.class)
class TimeWalkTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private TimeWalk cast() {
        return cast(player1);
    }

    private TimeWalk cast(Player caster) {
        TimeWalk timeWalk = new TimeWalk();
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(caster, timeWalk, "{1}{U}");
        harness.passBothPriorities();
        return timeWalk;
    }

    @Test
    @DisplayName("Resolving queues one extra turn for the caster")
    void resolvingQueuesOneExtraTurn() {
        cast();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The caster receives the extra turn")
    void casterReceivesExtraTurn() {
        cast(player2);

        assertThat(gd.extraTurns).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Normal turn order resumes after the extra turn")
    void normalTurnOrderResumes() {
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }

    @Test
    @DisplayName("Time Walk goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        TimeWalk timeWalk = cast();

        harness.assertInGraveyard(player1, "Time Walk");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(timeWalk);
        assertThat(gd.stack).isEmpty();
    }
}
