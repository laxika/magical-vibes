package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosianRally.class, FreshVolunteers.class, Plains.class})
class RamosianRallyTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1 until end of turn")
    void boostsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        castForMana();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        castForMana();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast for its alternate cost by tapping an untapped creature while controlling a Plains")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new RamosianRally()));

        harness.castWithAlternateCost(player1, 0, List.of(paymentCreature.getId()));
        harness.passBothPriorities();

        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getEffectivePower()).isEqualTo(3);
        assertThat(paymentCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(survivingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(survivingCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires control of a Plains")
    void alternateCostRequiresPlains() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new RamosianRally()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(paymentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Alternate cost requires an untapped creature")
    void alternateCostRequiresUntappedCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        paymentCreature.tap();
        harness.setHand(player1, List.of(new RamosianRally()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(paymentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Alternate cost cannot be paid by tapping a land")
    void alternateCostRequiresCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent paymentLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new RamosianRally()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(paymentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castForMana() {
        harness.castFromHand(player1, new RamosianRally(), "{3}{W}");
        harness.passBothPriorities();
    }
}
