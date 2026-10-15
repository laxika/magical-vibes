package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.cards.s.SeedbornMuse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheNinthDoctor.class, ArmageddonClock.class, TheThirteenthDoctor.class, SeedbornMuse.class})
class TheNinthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping The Ninth Doctor creates an additional upkeep")
    void untappingCreatesAdditionalUpkeep() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheNinthDoctor());
        doctor.tap();
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());

        advanceThroughUpkeeps(player1);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Ninth Doctor does not create an additional upkeep when it remains untapped")
    void remainsUntappedDoesNotCreateAdditionalUpkeep() {
        harness.addToBattlefield(player1, new TheNinthDoctor());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());

        advanceThroughUpkeeps(player1);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapping during an opponent's untap step does not create an additional upkeep")
    void opponentsUntapStepDoesNotCreateAdditionalUpkeep() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheNinthDoctor());
        doctor.tap();
        harness.addToBattlefield(player1, new SeedbornMuse());
        Permanent clock = harness.addToBattlefieldAndReturn(player2, new ArmageddonClock());

        advanceThroughUpkeeps(player2);

        assertThat(doctor.isTapped()).isFalse();
        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapping during the end step does not trigger The Ninth Doctor")
    void endStepUntapDoesNotTrigger() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheNinthDoctor());
        doctor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        doctor.tap();
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(doctor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceThroughUpkeeps(Player activePlayer) {
        gd.turnNumber = 2;
        harness.setLibrary(activePlayer, List.of(new TheNinthDoctor(), new TheNinthDoctor()));
        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(activePlayer);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
