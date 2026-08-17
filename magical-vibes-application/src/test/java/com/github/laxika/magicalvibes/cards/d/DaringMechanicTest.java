package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaringMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a target Mount")
    void putsCounterOnMount() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on a target Vehicle")
    void putsCounterOnVehicle() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a Mount or Vehicle")
    void rejectsOtherPermanentTypes() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
