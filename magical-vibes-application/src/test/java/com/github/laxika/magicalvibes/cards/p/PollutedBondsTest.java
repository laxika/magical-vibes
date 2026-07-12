package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PollutedBondsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's land entering drains 2 life and gains the controller 2 life")
    void opponentLandTriggers() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0); // plays the land via playCard

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Controller's own land entering does not trigger Polluted Bonds")
    void controllerLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
