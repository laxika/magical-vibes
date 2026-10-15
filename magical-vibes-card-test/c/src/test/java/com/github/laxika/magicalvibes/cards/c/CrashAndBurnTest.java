package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrashAndBurn.class, AirResponseUnit.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class})
class CrashAndBurnTest extends BaseCardTest {

    @Test
    void destroysTargetVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());

        cast(0, target);

        harness.assertInGraveyard(player2, "Air Response Unit");
    }

    @Test
    void dealsSixDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void dealsSixDamageToTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 8);

        cast(1, target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void eachModeRejectsAnIllegalTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> cast(0, forest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Vehicle");

        assertThatThrownBy(() -> cast(1, forest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    @Test
    void vehicleModeRejectsANonVehicleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Vehicle");
    }

    @Test
    void damageModeRejectsAnUncrewedVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());

        assertThatThrownBy(() -> cast(1, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    @Test
    void canDestroyYourOwnVehicleWithoutAffectingOtherVehicles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        harness.addToBattlefield(player2, new AirResponseUnit());

        cast(0, target);

        harness.assertInGraveyard(player1, "Air Response Unit");
        harness.assertNotOnBattlefield(player1, "Air Response Unit");
        harness.assertOnBattlefield(player2, "Air Response Unit");
        harness.assertNotInGraveyard(player2, "Air Response Unit");
    }

    @Test
    void marksExactlySixDamageOnASurvivingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        cast(1, target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void putsAPlaneswalkerWithSixLoyaltyIntoTheGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);

        cast(1, target);

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new CrashAndBurn()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
