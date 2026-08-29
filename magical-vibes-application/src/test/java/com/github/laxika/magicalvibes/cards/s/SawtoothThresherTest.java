package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Removing two +1/+1 counters gives it +4/+4 until end of turn")
    void removesCountersAndBoostsUntilEndOfTurn() {
        Permanent thresher = addReadyThresher(player1, 2);
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
    @DisplayName("The ability requires two +1/+1 counters")
    void cannotActivateWithOnlyOneCounter() {
        Permanent thresher = addReadyThresher(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyThresher(Player player, int counters) {
        Permanent thresher = new Permanent(new SawtoothThresher());
        thresher.setSummoningSick(false);
        thresher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(thresher);
        return thresher;
    }
}
