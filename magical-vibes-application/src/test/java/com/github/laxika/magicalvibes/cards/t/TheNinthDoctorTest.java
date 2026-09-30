package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TheNinthDoctor.class, ArmageddonClock.class, GrizzlyBears.class})
class TheNinthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping The Ninth Doctor creates an additional upkeep")
    void untappingCreatesAdditionalUpkeep() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheNinthDoctor());
        doctor.tap();
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());

        advanceUntilDoomCounters(player1, clock, 2);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Ninth Doctor does not create an additional upkeep when it remains untapped")
    void remainsUntappedDoesNotCreateAdditionalUpkeep() {
        harness.addToBattlefield(player1, new TheNinthDoctor());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());

        advanceUntilDoomCounters(player1, clock, 1);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    private void advanceUntilDoomCounters(Player activePlayer, Permanent clock, int expectedCount) {
        gd.turnNumber = 2;
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.performUntapStep(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        int attempts = 0;
        while (clock.getCounterCount(CounterType.DOOM) < expectedCount && attempts++ < 20) {
            harness.passBothPriorities();
        }

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(expectedCount);
    }
}
