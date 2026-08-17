package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HomaridTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a tide counter and gets -1/-1 with exactly one")
    void entersWithTideCounterAndWeakensWithOne() {
        Permanent homarid = castHomarid(player1);

        assertThat(homarid.getCounterCount(CounterType.TIDE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, homarid)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, homarid)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two tide counters give Homarid its normal power and toughness")
    void twoTideCountersGiveNormalStats() {
        Permanent homarid = addHomarid(player1);
        homarid.setCounterCount(CounterType.TIDE, 2);

        assertThat(gqs.getEffectivePower(gd, homarid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, homarid)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exactly three tide counters give Homarid +1/+1")
    void threeTideCountersStrengthenHomarid() {
        Permanent homarid = addHomarid(player1);
        homarid.setCounterCount(CounterType.TIDE, 3);

        assertThat(gqs.getEffectivePower(gd, homarid)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, homarid)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four tide counters are removed by the state-triggered ability")
    void fourTideCountersAreRemoved() {
        Permanent homarid = addHomarid(player1);
        homarid.setCounterCount(CounterType.TIDE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(homarid.getCounterCount(CounterType.TIDE)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(homarid.getCounterCount(CounterType.TIDE)).isZero();
    }

    private Permanent castHomarid(Player player) {
        harness.setHand(player, List.of(new Homarid()));
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Homarid");
    }

    private Permanent addHomarid(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Homarid());
    }
}
