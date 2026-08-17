package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoyagerGlidecar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HowlersHeavyTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives -3/-0 to a target opponent creature and draws a card")
    void cyclingDebuffsOpponentCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cycleHowlersHeavy(List.of(bears));

        assertThat(bears.getPowerModifier()).isEqualTo(-3);
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Howler's Heavy");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling can target a noncreature Vehicle")
    void cyclingDebuffsOpponentVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new VoyagerGlidecar());
        cycleHowlersHeavy(List.of(vehicle));

        assertThat(vehicle.getPowerModifier()).isEqualTo(-3);
        assertThat(vehicle.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cycling cannot target a creature you control")
    void cyclingCannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithNoLegalTargetStillDraws() {
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Howler's Heavy");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -3/-0 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cycleHowlersHeavy(List.of(bears));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    private void cycleHowlersHeavy(List<Permanent> targets) {
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, targets.isEmpty() ? null : targets.getFirst().getId());
        harness.passBothPriorities();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
