package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PollutedBonds.class, Forest.class, DevotedDruid.class, IvoryMask.class})
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
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve the trigger

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
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
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent's nonland permanent entering does not trigger Polluted Bonds")
    void opponentNonlandPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new DevotedDruid());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A land put onto the battlefield without being played still triggers")
    void opponentLandPutOntoBattlefieldTriggers() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Polluted Bonds triggers independently for an opponent's land")
    void multipleCopiesEachTrigger() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Polluted Bonds works when controlled by the second player")
    void secondPlayerControllerGainsLife() {
        harness.addToBattlefield(player2, new PollutedBonds());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Player shroud does not stop the nontargeted life loss or life gain")
    void opponentShroudDoesNotStopTrigger() {
        harness.addToBattlefield(player1, new PollutedBonds());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
