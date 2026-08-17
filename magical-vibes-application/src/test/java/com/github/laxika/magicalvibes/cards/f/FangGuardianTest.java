package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FangGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control +2/+2")
    void etbBoostsAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFangGuardian(bears);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB can target a Vehicle you control")
    void etbBoostsVehicleYouControl() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        castFangGuardian(vehicle);

        assertThat(vehicle.getPowerModifier()).isEqualTo(2);
        assertThat(vehicle.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void etbBoostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFangGuardian(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature or Vehicle you control");
    }

    @Test
    @DisplayName("Cannot target Fang Guardian itself")
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fang Guardian");
        assertThat(gd.stack).isEmpty();
    }

    private void castFangGuardian(Permanent target) {
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
