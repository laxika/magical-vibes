package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CaptureOfJingzhou.class)
class CaptureOfJingzhouTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private CaptureOfJingzhou cast() {
        return cast(player1);
    }

    private CaptureOfJingzhou cast(Player caster) {
        CaptureOfJingzhou capture = new CaptureOfJingzhou();
        harness.setHand(caster, List.of(capture));
        harness.addMana(caster, ManaColor.BLUE, 5);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(caster, 0, 0);
        return capture;
    }

    @Test
    @DisplayName("Resolving queues one extra turn for the caster")
    void resolvingQueuesOneExtraTurn() {
        cast();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The extra turn is taken by the caster after the current turn ends")
    void extraTurnTakenByCaster() {
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Normal turn order resumes after the single extra turn")
    void normalTurnOrderResumes() {
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn(); // extra turn
        advanceTurn(); // back to opponent

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }

    @Test
    @DisplayName("The player who casts Capture of Jingzhou receives the extra turn")
    void casterReceivesExtraTurn() {
        cast(player2);

        assertThat(gd.extraTurns).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Capture of Jingzhou goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        CaptureOfJingzhou capture = cast();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(capture);
        assertThat(gd.stack).isEmpty();
    }
}
