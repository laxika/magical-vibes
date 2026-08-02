package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GyreSageTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping with no +1/+1 counters produces no mana")
    void tapWithoutCountersProducesNoMana() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Tapping produces one green mana per +1/+1 counter")
    void tapProducesGreenManaPerCounter() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        sage.setSummoningSick(false);
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Evolve grows Gyre Sage and its mana ability scales with the counter")
    void evolveGrowsSageAndItsManaAbility() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        sage.setSummoningSick(false);

        // Grizzly Bears is 2/2 — greater power than the 1/2 Gyre Sage.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
