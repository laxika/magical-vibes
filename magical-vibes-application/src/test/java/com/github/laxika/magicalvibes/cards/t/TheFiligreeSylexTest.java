package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheFiligreeSylexTest extends BaseCardTest {

    @Test
    void putsAnOilCounterOnItself() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sylex.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void destroysNonlandsWithMatchingManaValueAcrossTheBattlefield() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        Permanent player1Bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        sylex.setCounterCount(CounterType.OIL, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Bear.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Bear.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elf);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        harness.assertInGraveyard(player1, "The Filigree Sylex");
    }

    @Test
    void removesOilCountersFromTheSylexBeforeSacrificingItToDealTenDamage() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        sylex.setCounterCount(CounterType.OIL, 10);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(sylex.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInGraveyard(player1, "The Filigree Sylex");
    }

    @Test
    void cannotActivateDamageAbilityWithoutTenOilCounters() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        sylex.setCounterCount(CounterType.OIL, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
