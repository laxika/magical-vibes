package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.d.DependableQuinjet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TruckToss.class, DuskLegionDreadnought.class, GrizzlyBears.class, DependableQuinjet.class})
class TruckTossTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a target player")
    void dealsFourDamageToPlayer() {
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsFourDamageToCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creatureId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs {2}{R}{R} without a Vehicle")
    void costsFullAmountWithoutVehicle() {
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs only {R}{R} while controlling a Vehicle")
    void costsReducedAmountWithVehicle() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's Vehicle does not reduce the cost")
    void opponentVehicleDoesNotReduceCost() {
        harness.addToBattlefield(player2, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A tapped, uncrewed Vehicle still reduces the cost")
    void tappedVehicleReducesCost() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DependableQuinjet());
        vehicle.tap();
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Vehicles do not reduce the colored mana requirement")
    void multipleVehiclesStillRequireTwoRedMana() {
        harness.addToBattlefieldAndReturn(player1, new DependableQuinjet()).tap();
        harness.addToBattlefieldAndReturn(player1, new DependableQuinjet()).tap();
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Vehicle in hand does not reduce the cost")
    void vehicleInHandDoesNotReduceCost() {
        harness.setHand(player1, List.of(new TruckToss(), new DependableQuinjet()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The caster may target themselves")
    void canDamageCaster() {
        harness.setHand(player1, List.of(new TruckToss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
