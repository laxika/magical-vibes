package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SterlingSupplier.class, GrizzlyBears.class})
class SterlingSupplierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another creature you control")
    void etbPutsCounterOnAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target the entering creature itself")
    void etbCannotTargetItself() {
        Permanent firstSupplier = harness.addToBattlefieldAndReturn(player1, new SterlingSupplier());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstSupplier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(firstSupplier.getId()))
                .singleElement()
                .extracting(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("ETB does nothing when there is no other creature you control")
    void etbDoesNothingWithoutAnotherCreature() {
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sterling Supplier");
        assertThat(gd.stack).isEmpty();
    }
}
