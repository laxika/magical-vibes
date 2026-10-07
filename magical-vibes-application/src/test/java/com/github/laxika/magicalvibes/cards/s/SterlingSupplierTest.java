package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SterlingSupplier.class, GrizzlyBears.class, Plains.class})
class SterlingSupplierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another creature you control")
    void etbPutsCounterOnAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
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
        harness.handlePermanentChosen(player1, firstSupplier.getId());
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

    @Test
    @DisplayName("ETB rejects itself, an opposing creature, and a land as targets")
    void etbRejectsIllegalTargets() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new SterlingSupplier());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SterlingSupplier());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(friendly.getId())
                        && !permanent.getId().equals(land.getId()))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, entering.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposing.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, friendly.getId());
        harness.passBothPriorities();

        assertThat(friendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB still resolves after its source leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingSupplier());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> !permanent.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not target an opponent's creature when no friendly target exists")
    void etbHasNoTargetWithOnlyOpposingCreature() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SterlingSupplier());
        harness.setHand(player1, List.of(new SterlingSupplier()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Sterling Supplier");
        assertThat(gd.stack).isEmpty();
    }
}
