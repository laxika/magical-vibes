package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SawtoothThresher.class)
class SawtoothThresherTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on it for each color spent")
    void sunburstPutsCountersForDistinctColorsSpent() {
        harness.setHand(player1, List.of(new SawtoothThresher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Sawtooth Thresher");
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sunburst counts each colored mana color only once")
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new SawtoothThresher()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Sawtooth Thresher");
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing two +1/+1 counters gives it +4/+4 until end of turn")
    void removesCountersAndBoostsUntilEndOfTurn() {
        Permanent thresher = addReadyThresher(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, thresher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thresher)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thresher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thresher)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated more than once without tapping")
    void canActivateAbilityMultipleTimes() {
        Permanent thresher = addReadyThresher(4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, thresher)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, thresher)).isEqualTo(9);
    }

    @Test
    @DisplayName("The ability requires two +1/+1 counters")
    void cannotActivateWithOnlyOneCounter() {
        Permanent thresher = addReadyThresher(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyThresher(int counters) {
        Permanent thresher = addCreatureReady(player1, new SawtoothThresher());
        thresher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return thresher;
    }
}
