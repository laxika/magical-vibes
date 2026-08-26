package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tidewalker.class, Island.class})
class TidewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with time counters equal to the Islands its controller controls")
    void entersWithIslandCountTimeCounters() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Tidewalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tidewalker = findPermanent(player1, "Tidewalker");
        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness track its time counters")
    void powerAndToughnessTrackTimeCounters() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 2);

        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(tidewalker.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tidewalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tidewalker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void sacrificesOnLastTimeCounter() {
        Permanent tidewalker = addCreatureReady(player1, new Tidewalker());
        tidewalker.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tidewalker");
        harness.assertInGraveyard(player1, "Tidewalker");
    }
}
