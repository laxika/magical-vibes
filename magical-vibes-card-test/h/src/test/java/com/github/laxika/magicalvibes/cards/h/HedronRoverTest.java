package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedronRover.class, Forest.class})
class HedronRoverTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Hedron Rover +2/+2 until end of turn")
    void landfallBoostsHedronRover() {
        Permanent rover = harness.addToBattlefieldAndReturn(player1, new HedronRover());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(rover.getEffectivePower()).isEqualTo(4);
        assertThat(rover.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Hedron Rover")
    void opponentLandDoesNotTrigger() {
        Permanent rover = harness.addToBattlefieldAndReturn(player1, new HedronRover());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(rover.getEffectivePower()).isEqualTo(2);
        assertThat(rover.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent rover = harness.addToBattlefieldAndReturn(player1, new HedronRover());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(rover.getEffectivePower()).isEqualTo(4);
        assertThat(rover.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rover.getEffectivePower()).isEqualTo(2);
        assertThat(rover.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A land entering without being played triggers landfall")
    void landEntryTriggersBeforeBoostResolves() {
        Permanent rover = harness.addToBattlefieldAndReturn(player1, new HedronRover());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(1);
        assertThat(rover.getEffectivePower()).isEqualTo(2);
        assertThat(rover.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(rover.getEffectivePower()).isEqualTo(4);
        assertThat(rover.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple landfall triggers accumulate until end of turn")
    void multipleLandfallTriggersStack() {
        Permanent rover = harness.addToBattlefieldAndReturn(player1, new HedronRover());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(rover.getEffectivePower()).isEqualTo(4);
        assertThat(rover.getEffectiveToughness()).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(rover.getEffectivePower()).isEqualTo(6);
        assertThat(rover.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rover.getEffectivePower()).isEqualTo(2);
        assertThat(rover.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each allied Rover boosts itself while an opposing Rover is unaffected")
    void landfallBoostsEachAlliedRoverOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HedronRover());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HedronRover());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HedronRover());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }
}
