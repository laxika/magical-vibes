package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntruderAlarmTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creatures do not untap during their controller's untap step")
    void creaturesStayTappedThroughUntapStep() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setSummoningSick(false);
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature entering untaps all creatures")
    void creatureEnteringUntapsAllCreatures() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setSummoningSick(false);
        giant.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve Grizzly Bears — enters, triggers Intruder Alarm
        harness.passBothPriorities(); // resolve the untap trigger

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering untaps all creatures")
    void opponentCreatureEnteringUntapsAllCreatures() {
        harness.addToBattlefield(player1, new IntruderAlarm());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setSummoningSick(false);
        giant.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
