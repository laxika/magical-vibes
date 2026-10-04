package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.g.GreenbeltGuardian;
import com.github.laxika.magicalvibes.cards.r.RoverBlades;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringMechanic.class, BrightfieldGlider.class, RoverBlades.class, GreenbeltGuardian.class})
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
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a Mount or Vehicle")
    void rejectsOtherPermanentTypes() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's uncrewed Vehicle")
    void targetsOpponentsVehicle() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new RoverBlades());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped summoning-sick Mechanic can activate repeatedly")
    void activatesRepeatedlyWhileTappedAndSummoningSick() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new DaringMechanic());
        mechanic.tap();
        mechanic.setSummoningSick(true);
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, mount.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(mechanic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires white mana to activate")
    void cannotPayWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
